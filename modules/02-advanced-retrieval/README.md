# Module 02: Advanced Retrieval

Complete RAG service implementations with enterprise audit logging.

## Services

### ReRankingRagService
Two-phase retrieval: retrieve broadly (Phase 1), rank carefully (Phase 2).
- Input: User question (String)
- Output: ReRankingResponse with rejection metrics
- Metrics: candidates_retrieved, chunksAfterReranking, rejectedByReranking, rejection_rate%

**Usage:**
```bash
curl -X POST http://localhost:8080/api/advanced-rag/reranking \
  -H "Content-Type: application/json" \
  -d '{"question":"Your question here"}'
```

### CompressingRagService
Query transformation for improved semantic matching.
- Input: User question (String)
- Output: CompressingResponse with transformation metrics
- Metrics: originalQuestion, transformedQuestion, chunksUsed, was_transformed

**Usage:**
```bash
curl -X POST http://localhost:8080/api/advanced-rag/compressing \
  -H "Content-Type: application/json" \
  -d '{"question":"Your question here"}'
```

### HybridSearchService
Combines BM25 (keyword) + Vector (semantic) search with alpha parameter.
- Input: User question (String)
- Output: HybridResponse with hybrid metrics
- Metrics: chunksRetrieved, alpha (0.0-1.0), context_found

**Usage:**
```bash
curl -X POST http://localhost:8080/api/advanced-rag/hybrid \
  -H "Content-Type: application/json" \
  -d '{"question":"Your question here"}'
```

## Audit Logging

All services inject `RetrievalAuditLog` and generate audit records with:
- Unique queryId (for tracing entire flow)
- Query metrics (candidates, rejection rates, transformations)
- Context sufficiency flags
- Timestamps in ISO 8601 UTC

Example log output:
```bash
[a3f2b1c9] [RERANKING-COMPLETE] candidates_retrieved=4 | candidates_after_reranking=1 | rejected_by_relevance_gate=3 | rejection_rate=75.0% | context_found=true | ts=2026-04-01T10:02:01.916544Z
```
## Architecture

Each service implements the port-adapter pattern with:
- **Port:** RetrievalAuditLog interface (defined in Module 01)
- **Adapters:** LoggingRetrievalAuditLog (SLF4J), future: JpaRetrievalAuditLog, KafkaRetrievalAuditLog

## Testing
```bash
# Run all Module 02 tests
gradle :02-advanced-retrieval:test

# Run integration tests
gradle :02-advanced-retrieval:integrationTest

# Run service locally
gradle bootRun
```

## Compliance

- **EU AI Act Article 13:** Full transparency through audit logging
- **Auditable decisions:** Each service logs why documents were selected/rejected
- **Decision traceability:** queryId allows tracking decisions through entire pipeline