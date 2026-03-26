package com.phoneix.advancedretrieval.reranking;

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

    public ReRankingRagService(ChatModel chatModel,
                               EmbeddingModel embeddingModel,
                               EmbeddingStore<TextSegment> embeddingStore) {
        this.chatModel = chatModel;

        // Fase 1 — retrieval amplio: 10 candidatos, umbral bajo
        this.retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(RETRIEVAL_CANDIDATES)
                .minScore(0.5)           // umbral bajo — dejamos pasar más candidatos
                .build();
    }

    public ReRankingResponse query(String userQuestion) {
        log.info("[RERANKING] Query: {}", userQuestion);

        // Fase 1: recuperar candidatos amplios
        List<Content> candidates = retriever.retrieve(Query.from(userQuestion));
        log.info("[RERANKING] Candidates retrieved: {}", candidates.size());

        // Fase 2: re-ranking local por relevancia semántica con el LLM
        // Para cada chunk, preguntamos al LLM si es relevante para la query
        List<Content> reranked = candidates.stream()
                .filter(c -> isRelevant(userQuestion, c.textSegment().text()))
                .collect(Collectors.toList());

        log.info("[RERANKING] After reranking: {} chunks (from {} candidates)",
                reranked.size(), candidates.size());

        if (reranked.isEmpty()) {
            return ReRankingResponse.noContext(userQuestion, candidates.size());
        }

        String context = reranked.stream()
                .map(c -> c.textSegment().text())
                .collect(Collectors.joining("\n\n---\n\n"));

        String answer = chatModel.chat(buildPrompt(userQuestion, context));

        return ReRankingResponse.of(userQuestion, answer,
                candidates.size(), reranked.size());
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
            boolean contextFound
    ) {
        static ReRankingResponse of(String q, String a, int candidates, int reranked) {
            return new ReRankingResponse(q, a, candidates, reranked, true);
        }

        static ReRankingResponse noContext(String q, int candidates) {
            return new ReRankingResponse(q,
                    "No sufficient context found after re-ranking.",
                    candidates, 0, false);
        }
    }
}