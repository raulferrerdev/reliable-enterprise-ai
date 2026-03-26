package com.phoneix.advancedretrieval.api;

import com.phoneix.advancedretrieval.hybrid.HybridSearchService;
import com.phoneix.advancedretrieval.reranking.ReRankingRagService;
import com.phoneix.advancedretrieval.querytransform.CompressingRagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advanced-rag")
public class AdvancedRagController {

    private final ReRankingRagService reRankingService;
    private final CompressingRagService compressingService;
    private final HybridSearchService hybridSearchService;

    public AdvancedRagController(ReRankingRagService reRankingService,
                                 CompressingRagService compressingService,
                                 HybridSearchService hybridSearchService) {
        this.reRankingService = reRankingService;
        this.compressingService = compressingService;
        this.hybridSearchService = hybridSearchService;
    }

    // POST /api/advanced-rag/reranking
    @PostMapping("/reranking")
    public ResponseEntity<?> queryWithReranking(@RequestBody QueryRequest request) {
        return ResponseEntity.ok(reRankingService.query(request.question()));
    }

    // POST /api/advanced-rag/compressing
    @PostMapping("/compressing")
    public ResponseEntity<?> queryWithCompressing(@RequestBody QueryRequest request) {
        return ResponseEntity.ok(compressingService.query(request.question()));
    }

    // POST /api/advanced-rag/hybrid
    @PostMapping("/hybrid")
    public ResponseEntity<?> queryWithHybrid(@RequestBody QueryRequest request) {
        return ResponseEntity.ok(hybridSearchService.query(request.question()));
    }

    public record QueryRequest(String question) {}
}
