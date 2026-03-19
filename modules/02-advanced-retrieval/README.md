# Module 02 · Advanced Retrieval

> Why cosine similarity is not enough for production.

## Techniques implemented

- **Hybrid search** — dense (embeddings) + sparse (BM25) with score fusion
- **Cross-encoder re-ranking** — reorder top-k results with a dedicated model
- **HyDE** — Hypothetical Document Embeddings for query expansion
- **Multi-query retrieval** — generate query variants to improve recall
- **Parent-document retrieval** — retrieve chunks, return full documents

Every technique is benchmarked against the Module 01 baseline. Results committed to the repo — improvements are reproducible and auditable.

## Status: 📋 Planned — S13–16
