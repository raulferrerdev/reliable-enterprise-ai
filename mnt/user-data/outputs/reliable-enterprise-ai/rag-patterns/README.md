# RAG Patterns — Reliable Enterprise AI

> Production-grade Retrieval-Augmented Generation patterns with LangChain4j and Weaviate.

---

## Why Most RAG Systems Fail in Production

The demo works. The production system doesn't. Here's why:

```
DEMO RAG                          PRODUCTION RAG
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Query → Embed → Search → LLM     Same pipeline, but:
                                  ❌ Retrieval quality degrades over time
                                  ❌ No re-ranking → wrong context wins
                                  ❌ No evaluation → silent hallucinations
                                  ❌ No hybrid search → keyword queries fail
                                  ❌ Context window overflow → LLM ignores docs
                                  ❌ No audit → can't explain any answer
```

This module provides patterns to fix each failure mode.

---

## Reliable RAG Architecture

```
┌──────────────┐
│  USER QUERY  │
└──────┬───────┘
       │
┌──────▼───────────────────────────┐
│           QUERY PROCESSOR        │
│  • Query expansion               │
│  • Intent classification         │
│  • Routing (RAG vs direct LLM)   │
└──────┬───────────────────────────┘
       │
┌──────▼───────────────────────────┐
│            RETRIEVER             │
│  • Dense retrieval (vectors)     │◄── Weaviate
│  • Sparse retrieval (BM25)       │
│  • Hybrid search (alpha tuning)  │
└──────┬───────────────────────────┘
       │
┌──────▼───────────────────────────┐
│            RE-RANKER             │
│  • Cross-encoder scoring         │
│  • Relevance filtering           │
│  • Diversity enforcement         │
└──────┬───────────────────────────┘
       │
┌──────▼───────────────────────────┐
│        CONTEXT BUILDER           │
│  • Token budget enforcement      │
│  • Context compression           │
│  • Source deduplication          │
└──────┬───────────────────────────┘
       │
┌──────▼───────────────────────────┐
│              LLM                 │◄── LangChain4j
│  • Augmented generation          │
│  • Structured output             │
└──────┬───────────────────────────┘
       │
┌──────▼───────────────────────────┐
│           EVALUATION             │
│  • Faithfulness check            │◄── Langfuse
│  • Answer relevancy score        │
│  • Context precision metric      │
└──────────────────────────────────┘
```

---

## Patterns

### 1. Basic RAG — `basic-rag/`

The baseline. LangChain4j + Weaviate. Useful as a starting point and benchmark.

```java
@Service
public class BasicRAGService {

    private final VectorStorePort vectorStore;
    private final LLMPort llm;
    private final AuditPort audit;

    public RAGResponse query(String userQuery, String sessionId) {

        // 1. Retrieve relevant documents
        List<Document> context = vectorStore.retrieve(userQuery, 5);

        // 2. Build augmented request
        LLMRequest request = LLMRequest.builder()
            .query(userQuery)
            .sessionId(sessionId)
            .build();

        // 3. Generate response with context
        LLMResponse response = llm.queryWithContext(request, context);

        // 4. Audit every interaction (non-negotiable in production)
        audit.record(AuditEvent.builder()
            .sessionId(sessionId)
            .query(userQuery)
            .retrievedDocuments(context)
            .response(response)
            .timestamp(Instant.now())
            .build());

        return RAGResponse.of(response, context);
    }
}
```

---

### 2. Hybrid Search — `hybrid-search/`

Combines dense (vector) and sparse (BM25) retrieval. Critical for enterprise documents with technical terms, product codes, or proper nouns that embeddings handle poorly.

```java
@Service
public class HybridSearchRAGService {

    private final VectorStorePort vectorStore;

    // alpha = 0.0 → pure BM25 (keyword)
    // alpha = 0.5 → balanced
    // alpha = 1.0 → pure vector (semantic)
    private static final double DEFAULT_ALPHA = 0.6;

    public List<Document> retrieve(String query) {
        return vectorStore.retrieveHybrid(query, 10, DEFAULT_ALPHA);
    }
}
```

**When to use hybrid over pure vector:**
- Queries with specific product names, codes, or IDs
- Technical documentation retrieval
- Multi-language environments
- When BM25 recall is historically higher for your domain

---

### 3. Re-ranking — `reranking/`

After retrieval, the top-k results by vector similarity are not always the most relevant. A cross-encoder re-ranker scores each (query, document) pair together.

```java
@Service
public class ReRankingService {

    private final CrossEncoderModel crossEncoder;
    private static final double RELEVANCE_THRESHOLD = 0.3;

    public List<Document> rerank(String query, List<Document> candidates) {

        return candidates.stream()
            .map(doc -> ScoredDocument.of(doc, crossEncoder.score(query, doc.getText())))
            .filter(scored -> scored.getScore() >= RELEVANCE_THRESHOLD)
            .sorted(Comparator.comparingDouble(ScoredDocument::getScore).reversed())
            .limit(5)
            .map(ScoredDocument::getDocument)
            .toList();
    }
}
```

**Impact:** Re-ranking typically improves faithfulness scores by 15-25% in production systems.

---

### 4. Context Compression — `context-compression/`

Reduces noise in the context window. Instead of passing entire documents, extract only the relevant sentences.

```java
@Service
public class ContextCompressionService {

    private final LLMPort llm;
    private static final int MAX_CONTEXT_TOKENS = 2000;

    public List<Document> compress(String query, List<Document> documents) {

        String compressionPrompt = """
            Given the question: "%s"
            
            Extract ONLY the sentences from the following document that are 
            directly relevant to answer the question. 
            Return nothing if the document is not relevant.
            
            Document:
            %s
            """;

        return documents.stream()
            .map(doc -> {
                LLMRequest req = LLMRequest.of(compressionPrompt.formatted(query, doc.getText()));
                LLMResponse compressed = llm.query(req);
                return Document.of(compressed.getText(), doc.getMetadata());
            })
            .filter(doc -> !doc.getText().isBlank())
            .toList();
    }
}
```

---

## RAG Failure Modes — Quick Reference

| Failure | Symptom | Pattern to apply |
|---------|---------|-----------------|
| Wrong documents retrieved | Irrelevant answers | Hybrid search + Re-ranking |
| Too much noise in context | LLM ignores context | Context compression |
| Hallucination on edge cases | Confident wrong answers | Evaluation pipeline |
| Stale knowledge base | Outdated answers | Drift detection (see governance) |
| No explainability | Can't audit answers | Audit trail (see governance) |

---

## Running the Examples

```bash
# Prerequisites: Java 17+, Docker

# Start Weaviate locally
docker run -d \
  -p 8080:8080 \
  -e QUERY_DEFAULTS_LIMIT=25 \
  -e AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED=true \
  cr.weaviate.io/semitechnologies/weaviate:latest

# Run basic RAG demo
cd rag-patterns/basic-rag
./mvnw spring-boot:run

# Ingest sample documents
curl -X POST http://localhost:8090/api/documents/ingest \
  -H "Content-Type: application/json" \
  -d '{"source": "docs/sample-enterprise-docs.pdf"}'

# Query
curl -X POST http://localhost:8090/api/query \
  -H "Content-Type: application/json" \
  -d '{"query": "What is the refund policy?"}'
```
