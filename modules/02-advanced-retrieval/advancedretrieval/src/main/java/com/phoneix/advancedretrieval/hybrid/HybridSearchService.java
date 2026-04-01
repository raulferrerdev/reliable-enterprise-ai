package com.phoneix.advancedretrieval.hybrid;

import com.phoneix.advancedretrieval.config.RetrievalAuditLog;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HybridSearchService {

    private static final Logger log = LoggerFactory.getLogger(HybridSearchService.class);

    private static final double HYBRID_ALPHA = 0.5;
    private static final int MAX_RESULTS = 5;
    private static final int MIN_CHUNKS_REQUIRED = 2;

    private final ChatModel chatModel;
    private final EmbeddingModel embeddingModel;
    private final WebClient weaviateClient;
    private final RetrievalAuditLog auditLog;

    @Value("${weaviate.object-class}")
    private String objectClass;

    public HybridSearchService(ChatModel chatModel,
                               EmbeddingModel embeddingModel,
                               @Value("${weaviate.scheme}") String scheme,
                               @Value("${weaviate.host}") String host,
                               RetrievalAuditLog auditLog) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.auditLog = auditLog;
        this.weaviateClient = WebClient.builder()
                .baseUrl(scheme + "://" + host)
                .build();
    }

    public HybridResponse query(String userQuestion) {
        String queryId = java.util.UUID.randomUUID().toString().substring(0, 8);
        log.info("[{}] [HYBRID] ===== HYBRID SEARCH START =====", queryId);
        log.info("[{}] [HYBRID] Question: {}", queryId, userQuestion);

        try {
            float[] queryVector = generateEmbedding(userQuestion);
            log.info("[{}] [HYBRID-VECTOR] Vector OK, dims: {}", queryId, queryVector.length);

            String graphqlQuery = buildHybridGraphQL(userQuestion, queryVector);
            log.info("[{}] [HYBRID-GRAPHQL] Built, length: {}", queryId, graphqlQuery.length());

            List<String> chunks = executeHybridSearch(graphqlQuery);
            log.info("[{}] [HYBRID-RESULTS] Chunks retrieved: {}", queryId, chunks.size());

            HybridResponse response;

            if (chunks.size() < MIN_CHUNKS_REQUIRED) {
                response = HybridResponse.noContext(userQuestion, chunks.size());
                log.warn("[{}] [HYBRID] Insufficient context: only {} chunks", queryId, chunks.size());
            } else {
                String context = String.join("\n\n---\n\n", chunks);
                String answer = chatModel.chat(buildPrompt(userQuestion, context));
                response = HybridResponse.of(userQuestion, answer, chunks.size(), HYBRID_ALPHA);
            }

            // ===== AUDIT LOGGING SECTION =====
            // Log hybrid search metrics
            log.info("[{}] [HYBRID-AUDIT] alpha={} | chunks_retrieved={} | context_found={}",
                    queryId,
                    response.alpha(),
                    response.chunksRetrieved(),
                    response.contextFound());

            // Full audit record
            log.info("""
                    [{}] [HYBRID-COMPLETE] \
                    question="{}" | \
                    chunks_retrieved={} | \
                    alpha={} | \
                    context_found={} | \
                    ts={}
                    """,
                    queryId,
                    userQuestion,
                    response.chunksRetrieved(),
                    response.alpha(),
                    response.contextFound(),
                    Instant.now()
            );

            // Inject into enterprise audit system
            auditLog.log(new RetrievalAuditLog.RetrievalEvent(
                    queryId,
                    response.question(),
                    response.chunksRetrieved(),
                    response.contextFound(),
                    response.answer(),
                    Instant.now()
            ));

            return response;

        } catch (Exception e) {
            log.error("[{}] [HYBRID] FAILED at: {}", queryId, e.getMessage(), e);
            return HybridResponse.noContext(userQuestion, 0);
        }
    }

    private float[] generateEmbedding(String text) {
        Response<Embedding> response = embeddingModel.embed(text);
        return response.content().vector();
    }

    private String buildHybridGraphQL(String query, float[] vector) {

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        sb.append("]");
        String vectorJson = sb.toString();

        return """
            {
              Get {
                %s(
                  hybrid: {
                    query: "%s"
                    vector: %s
                    alpha: %s
                  }
                  limit: %d
                ) {
                  text
                  _additional {
                    score
                  }
                }
              }
            }
            """.formatted(objectClass, escapeQuery(query), vectorJson,
                HYBRID_ALPHA, MAX_RESULTS);
    }

    @SuppressWarnings("unchecked")
    private List<String> executeHybridSearch(String graphqlQuery) {
        try {
            log.debug("[HYBRID] Sending GraphQL:\n{}", graphqlQuery);

            Map<String, Object> response = weaviateClient.post()
                    .uri("/v1/graphql")
                    .header("Content-Type", "application/json")
                    .bodyValue(Map.of("query", graphqlQuery))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                log.warn("[HYBRID] Null response from Weaviate");
                return List.of();
            }

            if (response.containsKey("errors")) {
                log.warn("[HYBRID] Weaviate errors: {}", response.get("errors"));
                return List.of();
            }

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Map<String, Object> get = (Map<String, Object>) data.get("Get");
            List<Map<String, Object>> results =
                    (List<Map<String, Object>>) get.get(objectClass);

            if (results == null || results.isEmpty()) {
                log.warn("[HYBRID] No results returned");
                return List.of();
            }

            return results.stream()
                    .map(r -> (String) r.get("text"))
                    .filter(t -> t != null && !t.isBlank())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("[HYBRID] Search failed: {}", e.getMessage(), e);
            return List.of();
        }
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

    private String escapeQuery(String query) {
        return query.replace("\"", "\\\"");
    }

    public record HybridResponse(
            String question,
            String answer,
            int chunksRetrieved,
            double alpha,
            boolean contextFound
    ) {
        /**
         * Factory method for successful hybrid search results.
         * Combines BM25 (keyword) + vector (semantic) retrieval.
         *
         * @param q the user's question
         * @param a the generated answer
         * @param chunks number of chunks retrieved
         * @param hybridAlpha the balance factor (0.0=pure BM25, 1.0=pure vector, 0.5=balanced)
         * @return HybridResponse with context found
         */
        static HybridResponse of(String q, String a, int chunks, double hybridAlpha) {
            return new HybridResponse(q, a, chunks, hybridAlpha, true);
        }

        /**
         * Factory method for failed hybrid search results.
         * Insufficient chunks retrieved.
         *
         * @param q the user's question
         * @param chunks number of chunks that were retrieved
         * @return HybridResponse with no context
         */
        static HybridResponse noContext(String q, int chunks) {
            return new HybridResponse(q,
                    "No sufficient context found via hybrid search.",
                    chunks, HYBRID_ALPHA, false);
        }
    }
}