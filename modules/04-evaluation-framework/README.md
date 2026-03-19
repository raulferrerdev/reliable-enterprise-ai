# Module 04 · Evaluation Framework

> You cannot improve what you cannot measure.

## Metrics implemented

| Metric | What it measures |
|--------|-----------------|
| Faithfulness | Does the answer stay grounded in retrieved context? |
| Answer Relevancy | Does the answer address the actual question? |
| Context Recall | Does the retrieved context contain necessary information? |
| Context Precision | Is the retrieved context free of irrelevant noise? |
| Latency | p50 / p95 / p99 response times |

## Automated evaluation pipeline

Every PR triggers the evaluation suite and compares against the baseline.
Regressions are flagged before merge.

## Status: 🔄 In progress — S17–18
