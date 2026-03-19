# Module 01 · RAG Foundations

> The baseline every enterprise RAG system needs before optimising anything.

## What this module covers

A production-grade RAG pipeline built with Java 21, Spring Boot 3, LangChain4j and Weaviate. No shortcuts. Fully tested. Fully observable.

## Pipeline

```
Document ingestion → Chunking → Embedding → Vector storage (Weaviate)
                                                      ↓
                              Response + metadata ← Generation ← Retrieval
```

## Key decisions documented

- Chunk size trade-offs for enterprise document types
- Embedding model selection criteria and cost/quality trade-offs
- Weaviate schema design for multi-tenant enterprise deployments
- Spring Boot integration patterns with LangChain4j

## Running locally

```bash
docker compose up -d        # Start Weaviate
./mvnw spring-boot:run      # Run the application
./mvnw test                 # Run tests
```

## Evaluation baseline

| Metric | Baseline |
|--------|----------|
| Faithfulness | TBD |
| Answer Relevancy | TBD |
| Context Recall | TBD |
| Latency p50 | TBD |
| Latency p99 | TBD |

## Status: 🔄 In progress
