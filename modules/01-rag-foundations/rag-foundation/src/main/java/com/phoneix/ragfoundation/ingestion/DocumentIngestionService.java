package com.phoneix.ragfoundation.ingestion;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    // Chunk size: 300 tokens, overlap: 30
    // En Enterprise RAG, el overlap evita cortar contexto en límites de chunk
    private static final int CHUNK_SIZE = 300;
    private static final int CHUNK_OVERLAP = 30;

    private final EmbeddingStoreIngestor ingestor;

    public DocumentIngestionService(EmbeddingModel embeddingModel,
                                    EmbeddingStore<TextSegment> embeddingStore) {
        this.ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .documentSplitter(
                        DocumentSplitters.recursive(CHUNK_SIZE, CHUNK_OVERLAP)
                )
                .build();
    }

    public void ingestFile(Path filePath) {
        log.info("Starting ingestion: {}", filePath.getFileName());
        Document document = FileSystemDocumentLoader.loadDocument(filePath);
        ingestor.ingest(document);
        log.info("Ingestion complete: {}", filePath.getFileName());
    }

    public void ingestDocuments(List<Document> documents) {
        log.info("Ingesting {} documents", documents.size());
        documents.forEach(ingestor::ingest);
        log.info("Batch ingestion complete");
    }
}
