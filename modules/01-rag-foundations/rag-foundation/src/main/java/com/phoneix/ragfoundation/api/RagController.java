package com.phoneix.ragfoundation.api;

import com.phoneix.ragfoundation.ingestion.DocumentIngestionService;
import com.phoneix.ragfoundation.retrieval.NaiveRagService;
import com.phoneix.ragfoundation.retrieval.NaiveRagService.RagResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final NaiveRagService ragService;
    private final DocumentIngestionService ingestionService;

    public RagController(NaiveRagService ragService,
                         DocumentIngestionService ingestionService) {
        this.ragService = ragService;
        this.ingestionService = ingestionService;
    }

    // POST /api/rag/query
    @PostMapping("/query")
    public ResponseEntity<RagResponse> query(@RequestBody QueryRequest request) {
        RagResponse response = ragService.query(request.question());
        return ResponseEntity.ok(response);
    }

    // POST /api/rag/ingest
    @PostMapping("/ingest")
    public ResponseEntity<String> ingest(@RequestBody IngestRequest request) {
        ingestionService.ingestFile(Path.of(request.filePath()));
        return ResponseEntity.ok("Document ingested successfully");
    }

    public record QueryRequest(String question) {}
    public record IngestRequest(String filePath) {}
}
