# Evaluation Pipelines — Reliable Enterprise AI

> The missing layer in most enterprise AI systems. If you can't measure it, you can't trust it.

---

## The Problem with "It Seems to Work"

```
Without evaluation:                With evaluation:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
❓ Did the model hallucinate?      ✅ Faithfulness: 0.91
❓ Was the answer relevant?        ✅ Answer Relevancy: 0.87
❓ Did we use good context?        ✅ Context Precision: 0.79
❓ Is it getting worse over time?  ✅ Weekly regression: -2% → alert
❓ Can we release this update?     ✅ Quality gate: PASSED
```

---

## Core Evaluation Metrics

### The RAG Triad

```
┌─────────────────────────────────────────────────────┐
│                   RAG EVALUATION TRIAD               │
│                                                     │
│   FAITHFULNESS          ANSWER            CONTEXT   │
│   Did the answer   ←  RELEVANCY  →      PRECISION  │
│   come from the        Was it useful    Did we      │
│   context?             to the user?     retrieve    │
│                                         correctly?  │
│   Score: 0.0-1.0       Score: 0.0-1.0  Score: 0.0-1.0
└─────────────────────────────────────────────────────┘
```

---

## Quality Gates for CI/CD

The key insight: **treat LLM quality like code quality**. Block deployments that degrade evaluation scores.

```java
@Component
public class LLMQualityGate {

    private static final double MIN_FAITHFULNESS    = 0.80;
    private static final double MIN_ANSWER_RELEVANCY = 0.75;
    private static final double MIN_CONTEXT_PRECISION = 0.70;

    private final EvaluationPort evaluator;

    public QualityGateResult evaluate(List<EvalCase> testCases) {

        List<EvaluationResult> results = testCases.stream()
            .map(evaluator::evaluate)
            .toList();

        double avgFaithfulness     = average(results, EvaluationResult::getFaithfulness);
        double avgAnswerRelevancy  = average(results, EvaluationResult::getAnswerRelevancy);
        double avgContextPrecision = average(results, EvaluationResult::getContextPrecision);

        boolean passed = avgFaithfulness     >= MIN_FAITHFULNESS
                      && avgAnswerRelevancy  >= MIN_ANSWER_RELEVANCY
                      && avgContextPrecision >= MIN_CONTEXT_PRECISION;

        return QualityGateResult.builder()
            .passed(passed)
            .faithfulness(avgFaithfulness)
            .answerRelevancy(avgAnswerRelevancy)
            .contextPrecision(avgContextPrecision)
            .testedCases(testCases.size())
            .build();
    }
}
```

---

## Evaluation Pipeline Architecture

```
┌─────────────────────────────────────────────────────┐
│                  EVAL DATASET                        │
│  { query, expected_answer, ground_truth_context }   │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│               RAG PIPELINE (under test)              │
│  query → retrieve → rerank → generate               │
└──────────────────────┬──────────────────────────────┘
                       │ { query, answer, context }
┌──────────────────────▼──────────────────────────────┐
│              EVALUATION ENGINE                       │
│                                                     │
│  ┌──────────────┐  ┌────────────────┐  ┌─────────┐ │
│  │ Faithfulness │  │Answer Relevancy│  │Context  │ │
│  │ Evaluator    │  │Evaluator       │  │Precision│ │
│  │ (LLM-judge)  │  │(embedding sim) │  │Evaluator│ │
│  └──────┬───────┘  └───────┬────────┘  └────┬────┘ │
│         └──────────────────┼────────────────┘      │
└──────────────────────────┬─┴────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────┐
│                 QUALITY GATE                         │
│  score ≥ threshold → PASS → deploy                  │
│  score < threshold → FAIL → block + alert           │
└──────────────────────────────────────────────────────┘
```

---

## Faithfulness Evaluator — Implementation

```java
@Component
public class FaithfulnessEvaluator {

    private final LLMPort llm;

    /**
     * Checks if each claim in the answer is supported by the retrieved context.
     * Uses LLM-as-a-judge pattern.
     */
    public double evaluate(String answer, List<Document> context) {

        String contextText = context.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n"));

        // Step 1: Extract claims from the answer
        List<String> claims = extractClaims(answer);

        // Step 2: Verify each claim against context
        long supportedClaims = claims.stream()
            .filter(claim -> isClaimSupported(claim, contextText))
            .count();

        return claims.isEmpty() ? 0.0 : (double) supportedClaims / claims.size();
    }

    private List<String> extractClaims(String answer) {
        String prompt = """
            Break the following answer into individual factual claims.
            Return each claim on a new line. Nothing else.
            
            Answer: %s
            """.formatted(answer);

        String response = llm.query(LLMRequest.of(prompt)).getText();
        return Arrays.asList(response.split("\n"));
    }

    private boolean isClaimSupported(String claim, String context) {
        String prompt = """
            Context: %s
            
            Claim: %s
            
            Is this claim fully supported by the context above?
            Answer with only YES or NO.
            """.formatted(context, claim);

        String response = llm.query(LLMRequest.of(prompt)).getText().trim();
        return response.equalsIgnoreCase("YES");
    }
}
```

---

## Hallucination Detection Experiment — `hallucination-detection/`

A structured experiment to measure hallucination rates in your specific domain.

```java
@Component
public class HallucinationDetectionExperiment {

    private final BasicRAGService ragService;
    private final FaithfulnessEvaluator evaluator;

    /**
     * Tests the pipeline against known ground-truth questions.
     * These are questions where we KNOW the correct answer from the documents.
     */
    public ExperimentResult run(List<GroundTruthCase> testCases) {

        List<CaseResult> results = testCases.stream()
            .map(tc -> {
                RAGResponse response = ragService.query(tc.getQuery(), "eval-session");

                double faithfulness = evaluator.evaluate(
                    response.getAnswer(),
                    response.getRetrievedDocuments()
                );

                boolean hallucinated = faithfulness < 0.5;

                return CaseResult.builder()
                    .query(tc.getQuery())
                    .expectedAnswer(tc.getExpectedAnswer())
                    .actualAnswer(response.getAnswer())
                    .faithfulness(faithfulness)
                    .hallucinated(hallucinated)
                    .build();
            })
            .toList();

        long hallucinatedCount = results.stream().filter(CaseResult::isHallucinated).count();

        return ExperimentResult.builder()
            .totalCases(testCases.size())
            .hallucinatedCases((int) hallucinatedCount)
            .hallucinationRate((double) hallucinatedCount / testCases.size())
            .results(results)
            .build();
    }
}
```

---

## Evaluation Test Dataset Format

```json
[
  {
    "query": "What is the maximum refund period?",
    "expected_answer": "30 days from purchase date",
    "ground_truth_context": "Customers may request a full refund within 30 days of purchase.",
    "category": "policy"
  },
  {
    "query": "Which countries are supported?",
    "expected_answer": "EU, UK, and USA",
    "ground_truth_context": "Our service is available in the European Union, United Kingdom, and United States.",
    "category": "coverage"
  }
]
```

---

## Minimum Viable Evaluation Setup

If you're starting from zero, implement these 3 things in order:

```
Week 1: Logging
  → Log every (query, context, answer) triplet to Langfuse
  → This is your evaluation dataset being built automatically

Week 2: Faithfulness check
  → Add FaithfulnessEvaluator to your production pipeline
  → Log the score alongside every answer

Week 3: Quality gate
  → Add 20 golden test cases to your CI pipeline
  → Block deployments that drop faithfulness below threshold
```

> **This is the difference between an AI system and a Reliable Enterprise AI system.**
