# Contributing to Reliable Enterprise AI

Thank you for your interest in contributing. This repository is the technical companion to the **Reliable Enterprise AI** framework — patterns and implementations for production-grade AI systems.

---

## What We're Looking For

Contributions that fit within the 5 Layers framework:

| Layer | Examples of good contributions |
|-------|-------------------------------|
| **Architecture** | New hexagonal patterns, DDD applications to AI systems |
| **RAG Patterns** | Retrieval strategies, context management, chunking approaches |
| **Evaluation** | New metrics, eval dataset formats, benchmarking tools |
| **Governance** | EU AI Act implementation patterns, audit trail improvements |
| **Diagrams** | New Mermaid diagrams illustrating AI system concepts |

---

## How to Contribute

### 1. Open a Discussion first

For significant contributions, **open a GitHub Discussion before a PR**. This avoids wasted effort and ensures alignment with the framework.

Good Discussion titles:
- "Pattern: Adaptive retrieval based on query complexity"
- "Question: Best approach for multi-tenant Weaviate audit trail"
- "Idea: Adding LangSmith as an alternative to Langfuse adapter"

### 2. Fork and implement

```bash
git clone https://github.com/raulferrerdev/reliable-enterprise-ai.git
git checkout -b feature/your-pattern-name
```

### 3. Code standards

This repository follows **Hexagonal Architecture** and **SOLID principles**:

```
✅ DO:
- Define interfaces (Ports) before implementations (Adapters)
- Keep domain logic free of framework dependencies
- Write unit tests for every component
- Document WHY, not just what

❌ DON'T:
- Import LangChain4j or Weaviate directly in domain classes
- Skip interfaces for adapters
- Write tests only for the happy path
```

### 4. Every code example needs a test

```java
// Pattern: FaithfulnessEvaluatorTest.java
class FaithfulnessEvaluatorTest {

    @Test
    void shouldReturnHighScoreWhenAnswerIsSupportedByContext() {
        // Given
        String answer = "The refund period is 30 days.";
        List<Document> context = List.of(
            Document.of("Customers may request a refund within 30 days of purchase.")
        );

        // When
        double score = evaluator.evaluate(answer, context);

        // Then
        assertThat(score).isGreaterThanOrEqualTo(0.8);
    }

    @Test
    void shouldReturnLowScoreWhenAnswerContainsHallucination() {
        // Given
        String answer = "The refund period is 90 days and includes free shipping.";
        List<Document> context = List.of(
            Document.of("Customers may request a refund within 30 days of purchase.")
        );

        // When
        double score = evaluator.evaluate(answer, context);

        // Then
        assertThat(score).isLessThan(0.5);
    }
}
```

### 5. Submit your PR

PR description should include:
- **Problem**: What failure mode or gap does this address?
- **Solution**: What pattern or implementation does this add?
- **Layer**: Which of the 5 layers does it belong to?
- **Tests**: Confirmation that tests are included

---

## Code of Conduct

- Be specific and technical in discussions
- Cite sources for benchmarks and performance claims
- Respect that this is a framework with a defined scope

---

## Questions?

Open a [GitHub Discussion](../../discussions) — questions are contributions too.
