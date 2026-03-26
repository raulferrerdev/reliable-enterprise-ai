package com.phoneix.ragfoundation.retrieval;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NaiveRagService {

    private static final Logger log = LoggerFactory.getLogger(NaiveRagService.class);
    private static final int MAX_RESULTS = 5;
    private static final double MIN_SCORE = 0.75;
    private static final int MIN_CHUNKS_REQUIRED = 2;

    private final ChatModel chatModel;
    private final ContentRetriever retriever;

    public NaiveRagService(ChatModel chatModel,
                           EmbeddingModel embeddingModel,
                           EmbeddingStore<TextSegment> embeddingStore) {
        this.chatModel = chatModel;
        this.retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(MAX_RESULTS)
                .minScore(MIN_SCORE)
                .build();
    }

    public RagResponse query(String userQuestion) {
        log.info("RAG query received: {}", userQuestion);

        List<Content> retrievedContents = retriever.retrieve(
                Query.from(userQuestion)
        );

        if (retrievedContents.isEmpty() || retrievedContents.size() < MIN_CHUNKS_REQUIRED) {
            log.warn("No relevant context found for query: {}", userQuestion);
            return RagResponse.noContextFound(userQuestion);
        }

        String context = retrievedContents.stream()
                .map(c -> c.textSegment().text())
                .collect(Collectors.joining("\n\n---\n\n"));

        String augmentedPrompt = buildPrompt(userQuestion, context);

        String answer = chatModel.chat(augmentedPrompt);

        log.info("RAG response generated. Chunks used: {}", retrievedContents.size());

        return RagResponse.of(userQuestion, answer, retrievedContents.size());
    }

    private String buildPrompt(String question, String context) {
        return """
                You are an enterprise AI assistant. Answer the question using ONLY
                the context provided below. If the context does not contain enough
                information to answer, say so explicitly — do not invent information.

                CONTEXT:
                %s

                QUESTION:
                %s

                ANSWER:
                """.formatted(context, question);
    }

    public record RagResponse(
            String question,
            String answer,
            int chunksUsed,
            boolean contextFound
    ) {
        static RagResponse of(String q, String a, int chunks) {
            return new RagResponse(q, a, chunks, true);
        }

        static RagResponse noContextFound(String q) {
            return new RagResponse(q,
                    "No sufficient context found to answer this question reliably.",
                    0, false);
        }
    }
}