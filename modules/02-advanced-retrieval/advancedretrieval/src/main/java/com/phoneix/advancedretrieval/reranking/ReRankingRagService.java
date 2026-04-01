package com.phoneix.advancedretrieval.reranking;

import com.phoneix.advancedretrieval.config.RetrievalAuditLog;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import dev.langchain4j.rag.content.Content;

@Service
public class ReRankingRagService {

    private static final Logger log = LoggerFactory.getLogger(ReRankingRagService.class);

    // Fase 1: recupera más candidatos de los necesarios
    private static final int RETRIEVAL_CANDIDATES = 10;
    // Fase 2: el re-ranker selecciona solo los mejores
    private static final double RERANK_MIN_SCORE = 0.8;

    private final ChatModel chatModel;
    private final ContentRetriever retriever;
    private final RetrievalAuditLog auditLog;

    public ReRankingRagService(ChatModel chatModel,
                               EmbeddingModel embeddingModel,
                               EmbeddingStore<TextSegment> embeddingStore,
                               RetrievalAuditLog auditLog) {
        this.chatModel = chatModel;
        this.auditLog = auditLog;

        // Fase 1 — retrieval amplio: 10 candidatos, umbral bajo
        this.retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(RETRIEVAL_CANDIDATES)
                .minScore(0.5)           // umbral bajo — dejamos pasar más candidatos
                .build();
    }

    public ReRankingResponse query(String userQuestion) {
        String queryId = java.util.UUID.randomUUID().toString().substring(0, 8);
        log.info("[{}] [RERANKING] Query: {}", queryId, userQuestion);

        // Fase 1: recuperar candidatos amplios
        List<Content> candidates = retriever.retrieve(Query.from(userQuestion));
        log.info("[{}] [RERANKING-PHASE1] Candidates retrieved: {}", queryId, candidates.size());

        // Fase 2: re-ranking local por relevancia semántica con el LLM
        // Para cada chunk, preguntamos al LLM si es relevante para la query
        List<Content> reranked = candidates.stream()
                .filter(c -> isRelevant(userQuestion, c.textSegment().text()))
                .collect(Collectors.toList());

        log.info("[{}] [RERANKING-PHASE2] After reranking: {} chunks (from {} candidates)",
                queryId, reranked.size(), candidates.size());

        ReRankingResponse response;

        if (reranked.isEmpty()) {
            response = ReRankingResponse.noContext(userQuestion, candidates.size());
        } else {
            String context = reranked.stream()
                    .map(c -> c.textSegment().text())
                    .collect(Collectors.joining("\n\n---\n\n"));

            String answer = chatModel.chat(buildPrompt(userQuestion, context));
            response = ReRankingResponse.of(userQuestion, answer,
                    candidates.size(), reranked.size());
        }

        // ===== AUDIT LOGGING SECTION =====
        // Log rejection metrics
        log.info("[{}] [RERANKING-AUDIT] rejection_rate={}%",
                queryId, response.rejectionRatePercent());

        // Detect and log if re-ranker had significant filtering impact
        if (response.hadSignificantFiltering()) {
            log.warn("[{}] [RERANKING-IMPACT] Re-ranker filtered >50% of candidates ({}%)",
                    queryId, response.rejectionRatePercent());
        }

        // Full audit record
        log.info("""
                [{}] [RERANKING-COMPLETE] \
                candidates_retrieved={} | \
                candidates_after_reranking={} | \
                rejected_by_relevance_gate={} | \
                rejection_rate={}% | \
                context_found={} | \
                ts={}
                """,
                queryId,
                response.candidatesRetrieved(),
                response.chunksAfterReranking(),
                response.rejectedByReranking(),
                response.rejectionRatePercent(),
                response.contextFound(),
                Instant.now()
        );

        // Inject into enterprise audit system (if needed)
        auditLog.log(new RetrievalAuditLog.RetrievalEvent(
                queryId,
                response.question(),
                response.chunksAfterReranking(),
                response.contextFound(),
                response.answer(),
                Instant.now()
        ));

        return response;
    }

    // Re-ranker local: el LLM evalúa si el chunk es relevante
    // En producción se reemplazaría por Cohere Rerank o similar
    private boolean isRelevant(String question, String chunk) {
        String prompt = """
                Is the following text relevant to answer this question?
                Question: %s
                Text: %s
                Reply with only YES or NO.
                """.formatted(question, chunk.substring(0, Math.min(200, chunk.length())));

        String response = chatModel.chat(prompt).trim().toUpperCase();
        return response.startsWith("YES");
    }

    private String buildPrompt(String question, String context) {
        return """
                You are an enterprise AI assistant. Answer ONLY using the context below.
                If the context is insufficient, say so explicitly.
 
                CONTEXT:
                %s
 
                QUESTION:
                %s
 
                ANSWER:
                """.formatted(context, question);
    }

    public record ReRankingResponse(
            String question,
            String answer,
            int candidatesRetrieved,
            int chunksAfterReranking,
            int rejectedByReranking,
            boolean contextFound
    ) {
        /**
         * Factory method for successful re-ranking results.
         * Automatically calculates rejectedByReranking from candidates and reranked count.
         *
         * @param q the user's question
         * @param a the generated answer
         * @param candidates total candidates retrieved in phase 1
         * @param reranked candidates that passed re-ranking gate in phase 2
         * @return ReRankingResponse with context found and rejection count calculated
         */
        static ReRankingResponse of(String q, String a, int candidates, int reranked) {
            return new ReRankingResponse(
                    q,
                    a,
                    candidates,
                    reranked,
                    candidates - reranked,  // rejected = total - passed
                    true
            );
        }

        /**
         * Factory method for failed re-ranking results.
         * No documents passed the relevance gate.
         *
         * @param q the user's question
         * @param candidates total candidates retrieved in phase 1
         * @return ReRankingResponse with no context and all candidates rejected
         */
        static ReRankingResponse noContext(String q, int candidates) {
            return new ReRankingResponse(
                    q,
                    "No sufficient context found after re-ranking.",
                    candidates,
                    0,           // no chunks survived re-ranking
                    candidates,  // all were rejected
                    false
            );
        }

        /**
         * Convenience method to get the rejection rate as a percentage.
         * Useful for logging and analytics.
         *
         * @return rejection percentage (0-100)
         */
        public double rejectionRatePercent() {
            if (candidatesRetrieved == 0) {
                return 0.0;
            }
            return (rejectedByReranking * 100.0) / candidatesRetrieved;
        }

        /**
         * Convenience method to check if re-ranking filtered out the majority of candidates.
         * Useful for performance analysis and alerting.
         *
         * @return true if more than 50% of candidates were rejected
         */
        public boolean hadSignificantFiltering() {
            return rejectionRatePercent() > 50.0;
        }
    }
}