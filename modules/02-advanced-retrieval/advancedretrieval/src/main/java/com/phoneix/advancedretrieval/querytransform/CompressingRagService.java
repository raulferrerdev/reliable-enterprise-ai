package com.phoneix.advancedretrieval.querytransform;

import com.phoneix.advancedretrieval.config.RetrievalAuditLog;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompressingRagService {

    private static final Logger log =
            LoggerFactory.getLogger(CompressingRagService.class);

    private final ChatModel chatModel;
    private final ContentRetriever retriever;
    private final RetrievalAuditLog auditLog;

    public CompressingRagService(ChatModel chatModel,
                                 EmbeddingModel embeddingModel,
                                 EmbeddingStore<TextSegment> embeddingStore,
                                 RetrievalAuditLog auditLog) {
        this.chatModel = chatModel;
        this.auditLog = auditLog;
        this.retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(5)
                .minScore(0.75)
                .build();
    }

    public CompressingResponse query(String userQuestion) {
        String queryId = java.util.UUID.randomUUID().toString().substring(0, 8);
        log.info("[{}] [COMPRESSING] Original query: {}", queryId, userQuestion);

        // Query Transformation: comprime y reformula la pregunta
        // para mejorar el matching con los documentos indexados
        String transformedQuery = transformQuery(userQuestion);
        log.info("[{}] [COMPRESSING] Transformed query: {}", queryId, transformedQuery);

        List<Content> contents = retriever.retrieve(Query.from(transformedQuery));
        log.info("[{}] [COMPRESSING] Retrieved chunks: {}", queryId, contents.size());

        CompressingResponse response;

        if (contents.size() < 2) {
            response = CompressingResponse.noContext(userQuestion, transformedQuery);
            log.warn("[{}] [COMPRESSING] Insufficient context: only {} chunks", queryId, contents.size());
        } else {
            String context = contents.stream()
                    .map(c -> c.textSegment().text())
                    .collect(Collectors.joining("\n\n---\n\n"));

            String answer = chatModel.chat(buildPrompt(userQuestion, context));
            response = CompressingResponse.of(userQuestion, transformedQuery,
                    answer, contents.size());
        }

        // ===== AUDIT LOGGING SECTION =====
        // Log query transformation metrics
        boolean queryWasTransformed = !userQuestion.equals(transformedQuery);
        log.info("[{}] [COMPRESSING-TRANSFORM] original_length={} | transformed_length={} | was_transformed={}",
                queryId,
                userQuestion.length(),
                transformedQuery.length(),
                queryWasTransformed);

        // Full audit record
        log.info("""
                [{}] [COMPRESSING-COMPLETE] \
                original_query="{}" | \
                transformed_query="{}" | \
                chunks_retrieved={} | \
                context_found={} | \
                ts={}
                """,
                queryId,
                userQuestion,
                transformedQuery,
                response.chunksUsed(),
                response.contextFound(),
                Instant.now()
        );

        // Inject into enterprise audit system
        auditLog.log(new RetrievalAuditLog.RetrievalEvent(
                queryId,
                response.originalQuestion(),
                response.chunksUsed(),
                response.contextFound(),
                response.answer(),
                Instant.now()
        ));

        return response;
    }

    // Transforma la query para mejorar el retrieval
    // Versión local de CompressingQueryTransformer sin dependencia de cloud
    private String transformQuery(String originalQuery) {
        String prompt = """
                Rewrite the following question to make it more specific and
                suitable for searching in an enterprise document corpus.
                Return ONLY the rewritten question, nothing else.
                
                Original question: %s
                Rewritten question:
                """.formatted(originalQuery);

        return chatModel.chat(prompt).trim();
    }

    private String buildPrompt(String question, String context) {
        return """
                You are an enterprise AI assistant. Answer ONLY using the context below.
                If insufficient, say so explicitly.
 
                CONTEXT:
                %s
 
                QUESTION:
                %s
 
                ANSWER:
                """.formatted(context, question);
    }

    public record CompressingResponse(
            String originalQuestion,
            String transformedQuestion,
            String answer,
            int chunksUsed,
            boolean contextFound
    ) {
        /**
         * Factory method for successful query compression results.
         *
         * @param orig the original user question
         * @param transformed the LLM-rewritten question
         * @param answer the generated answer
         * @param chunks number of chunks retrieved
         * @return CompressingResponse with context found
         */
        static CompressingResponse of(String orig, String transformed,
                                      String answer, int chunks) {
            return new CompressingResponse(orig, transformed, answer, chunks, true);
        }

        /**
         * Factory method for failed compression results.
         * Insufficient context after transformation.
         *
         * @param orig the original user question
         * @param transformed the LLM-rewritten question
         * @return CompressingResponse with no context
         */
        static CompressingResponse noContext(String orig, String transformed) {
            return new CompressingResponse(orig, transformed,
                    "No sufficient context found.", 0, false);
        }
    }
}
