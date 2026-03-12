# Architecture — Reliable Enterprise AI

> Architectural patterns for production-grade AI systems using Hexagonal Architecture and SOLID principles.

---

## Why Architecture Matters More Than Models

The most common failure in enterprise AI is not choosing the wrong model — it's building a system with no clear boundaries, no testability, and no path to production reliability.

This module applies **Hexagonal Architecture** (Ports & Adapters) to AI systems, isolating the LLM and vector database as external adapters — not core dependencies.

---

## Hexagonal AI System

```
                    ┌─────────────────────────────┐
                    │         DOMAIN CORE          │
                    │                              │
    REST API ──────►│  AIQueryService              │
    CLI      ──────►│  DocumentIngestionService    │
                    │  EvaluationService           │
                    │                              │
                    │         PORTS                │
                    │  ┌──────────────────────┐   │
                    │  │ LLMPort (interface)  │   │
                    │  │ VectorStorePort      │   │
                    │  │ EvaluationPort       │   │
                    │  │ AuditPort            │   │
                    │  └──────────────────────┘   │
                    └───────────┬─────────────────┘
                                │
              ┌─────────────────┼──────────────────┐
              │                 │                  │
    ┌─────────▼──────┐ ┌───────▼───────┐ ┌───────▼───────┐
    │  LangChain4j   │ │   Weaviate    │ │   Langfuse    │
    │  Adapter       │ │   Adapter     │ │   Adapter     │
    └────────────────┘ └───────────────┘ └───────────────┘
```

---

## Core Interfaces (Ports)

```java
// Port: what the domain needs from any LLM
public interface LLMPort {
    LLMResponse query(LLMRequest request);
    LLMResponse queryWithContext(LLMRequest request, List<Document> context);
}

// Port: what the domain needs from any vector store
public interface VectorStorePort {
    void store(List<Document> documents);
    List<Document> retrieve(String query, int topK);
    List<Document> retrieveHybrid(String query, int topK, double alpha);
}

// Port: evaluation capability
public interface EvaluationPort {
    EvaluationResult evaluate(LLMRequest request, LLMResponse response, List<Document> context);
}

// Port: audit & traceability
public interface AuditPort {
    void record(AuditEvent event);
    List<AuditEvent> getTrace(String sessionId);
}
```

---

## LangChain4j Adapter (example)

```java
@Component
public class LangChain4jLLMAdapter implements LLMPort {

    private final ChatLanguageModel model;

    public LangChain4jLLMAdapter(ChatLanguageModel model) {
        this.model = model;
    }

    @Override
    public LLMResponse query(LLMRequest request) {
        String response = model.generate(request.getPrompt());
        return LLMResponse.of(response);
    }

    @Override
    public LLMResponse queryWithContext(LLMRequest request, List<Document> context) {
        String augmentedPrompt = buildPromptWithContext(request.getPrompt(), context);
        String response = model.generate(augmentedPrompt);
        return LLMResponse.of(response);
    }

    private String buildPromptWithContext(String query, List<Document> context) {
        String contextText = context.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n---\n"));

        return """
            Answer the question based ONLY on the following context.
            If the context does not contain the answer, say "I don't know."
            
            Context:
            %s
            
            Question: %s
            """.formatted(contextText, query);
    }
}
```

---

## Weaviate Adapter (example)

```java
@Component
public class WeaviateVectorStoreAdapter implements VectorStorePort {

    private final WeaviateClient client;
    private final EmbeddingModel embeddingModel;
    private static final String CLASS_NAME = "EnterpriseDocument";

    @Override
    public List<Document> retrieve(String query, int topK) {
        float[] queryEmbedding = embeddingModel.embed(query).content().vector();

        Result<GraphQLResponse> result = client.graphQL().get()
            .withClassName(CLASS_NAME)
            .withNearVector(new NearVectorArgument.Builder()
                .vector(queryEmbedding)
                .build())
            .withLimit(topK)
            .withFields(new Field("content"), new Field("source"), new Field("_additional { certainty }"))
            .run();

        return parseDocuments(result);
    }

    @Override
    public List<Document> retrieveHybrid(String query, int topK, double alpha) {
        // alpha=1.0 → pure vector, alpha=0.0 → pure BM25
        Result<GraphQLResponse> result = client.graphQL().get()
            .withClassName(CLASS_NAME)
            .withHybrid(new HybridArgument.Builder()
                .query(query)
                .alpha((float) alpha)
                .build())
            .withLimit(topK)
            .withFields(new Field("content"), new Field("source"))
            .run();

        return parseDocuments(result);
    }
}
```

---

## Patterns in This Module

| Pattern | Description | Status |
|---------|-------------|--------|
| `hexagonal-ai-system/` | Full working example | 🔜 Coming |
| `patterns/rag-circuit-breaker` | Fallback when retrieval fails | 🔜 Coming |
| `patterns/prompt-versioning` | Version prompts like code | 🔜 Coming |
| `patterns/context-window-budget` | Token budget management | 🔜 Coming |

---

## Key Principle

> **The LLM and the vector database are infrastructure, not architecture.**  
> They should be swappable without changing your domain logic.

This is the same principle that made Spring great for database access — and it applies equally to AI systems.
