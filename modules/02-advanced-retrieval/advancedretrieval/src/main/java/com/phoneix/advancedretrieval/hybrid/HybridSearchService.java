package com.phoneix.advancedretrieval.hybrid;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

    @Value("${weaviate.object-class}")
    private String objectClass;

    public HybridSearchService(ChatModel chatModel,
                               EmbeddingModel embeddingModel,
                               @Value("${weaviate.scheme}") String scheme,
                               @Value("${weaviate.host}") String host) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.weaviateClient = WebClient.builder()
                .baseUrl(scheme + "://" + host)
                .build();
    }

    public HybridResponse query(String userQuestion) {
        log.info("[HYBRID] ===== HYBRID SEARCH START =====");
        log.info("[HYBRID] Question: {}", userQuestion);

        try {
            float[] queryVector = generateEmbedding(userQuestion);
            log.info("[HYBRID] Vector OK, dims: {}", queryVector.length);

            String graphqlQuery = buildHybridGraphQL(userQuestion, queryVector);
            log.info("[HYBRID] GraphQL built, length: {}", graphqlQuery.length());

            List<String> chunks = executeHybridSearch(graphqlQuery);
            log.info("[HYBRID] Chunks: {}", chunks.size());

            if (chunks.size() < MIN_CHUNKS_REQUIRED) {
                return HybridResponse.noContext(userQuestion, chunks.size());
            }

            String context = String.join("\n\n---\n\n", chunks);
            String answer = chatModel.chat(buildPrompt(userQuestion, context));
            return HybridResponse.of(userQuestion, answer, chunks.size(), HYBRID_ALPHA);

        } catch (Exception e) {
            log.error("[HYBRID] FAILED at: {}", e.getMessage(), e);
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
        static HybridResponse of(String q, String a, int chunks, double alpha) {
            return new HybridResponse(q, a, chunks, alpha, true);
        }

        static HybridResponse noContext(String q, int chunks) {
            return new HybridResponse(q,
                    "No sufficient context found via hybrid search.",
                    chunks, HYBRID_ALPHA, false);
        }
    }
}