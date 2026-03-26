package com.phoneix.advancedretrieval.querytransform;

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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompressingRagService {

    private static final Logger log =
            LoggerFactory.getLogger(CompressingRagService.class);

    private final ChatModel chatModel;
    private final ContentRetriever retriever;

    public CompressingRagService(ChatModel chatModel,
                                 EmbeddingModel embeddingModel,
                                 EmbeddingStore<TextSegment> embeddingStore) {
        this.chatModel = chatModel;
        this.retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(5)
                .minScore(0.75)
                .build();
    }

    public CompressingResponse query(String userQuestion) {
        log.info("[COMPRESSING] Original query: {}", userQuestion);

        // Query Transformation: comprime y reformula la pregunta
        // para mejorar el matching con los documentos indexados
        String transformedQuery = transformQuery(userQuestion);
        log.info("[COMPRESSING] Transformed query: {}", transformedQuery);

        List<Content> contents = retriever.retrieve(Query.from(transformedQuery));

        if (contents.size() < 2) {
            return CompressingResponse.noContext(userQuestion, transformedQuery);
        }

        String context = contents.stream()
                .map(c -> c.textSegment().text())
                .collect(Collectors.joining("\n\n---\n\n"));

        String answer = chatModel.chat(buildPrompt(userQuestion, context));

        return CompressingResponse.of(userQuestion, transformedQuery,
                answer, contents.size());
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
        static CompressingResponse of(String orig, String transformed,
                                      String answer, int chunks) {
            return new CompressingResponse(orig, transformed, answer, chunks, true);
        }

        static CompressingResponse noContext(String orig, String transformed) {
            return new CompressingResponse(orig, transformed,
                    "No sufficient context found.", 0, false);
        }
    }
}
