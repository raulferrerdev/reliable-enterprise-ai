# The Reliable Enterprise AI Checklist

> **Free resource by Raúl Ferrer** — Reliable Enterprise AI Architect  
> Use this checklist to audit any enterprise AI system before production deployment.

---

## How to Use This Checklist

Score each section independently. A system is **production-ready** when all sections score ≥ 80%.

```
✅ Implemented and verified
⚠️  Partially implemented
❌  Not implemented
N/A Not applicable to this system
```

---

## Section 1 — Architecture (8 items)

```
□ Domain logic is isolated from LLM and vector database dependencies
□ LLM is accessed through an interface (Port), not directly
□ Vector store is accessed through an interface (Port), not directly
□ Switching the LLM provider requires no domain changes
□ Switching the vector database requires no domain changes
□ Each component has a single, well-defined responsibility (SRP)
□ The system degrades gracefully when the LLM is unavailable
□ Prompt templates are versioned and stored as code, not hardcoded
```

**Section score: __ / 8**

---

## Section 2 — RAG Quality (12 items)

```
□ Hybrid search is implemented (dense + sparse retrieval)
□ Re-ranking is applied after initial retrieval
□ Context window token budget is explicitly managed
□ Context compression is applied to reduce noise
□ Retrieval quality is measured (not just assumed)
□ Chunk size has been empirically validated for your domain
□ Chunk overlap is configured appropriately
□ Documents are re-indexed when source content changes
□ Retrieval latency is monitored and within SLA
□ Fallback behavior is defined when retrieval returns no results
□ Multi-query retrieval is used for complex questions
□ Source documents are included in every response
```

**Section score: __ / 12**

---

## Section 3 — Evaluation (6 items)

```
□ Faithfulness is measured for every production response
□ Answer relevancy is measured for every production response
□ A golden dataset of test cases exists for your domain
□ Quality gates are integrated into the CI/CD pipeline
□ Regression testing runs on every model or prompt change
□ Weekly evaluation reports are reviewed by the team
```

**Section score: __ / 6**

---

## Section 4 — Observability (8 items)

```
□ Every inference is logged (query, context, answer, scores)
□ Latency is measured at each pipeline stage
□ Token usage is tracked and alerted on anomalies
□ Error rates are monitored with alerting thresholds
□ Evaluation scores are tracked over time (trend visible)
□ A dashboard exists showing system health
□ Alerts are configured for quality degradation
□ Runbooks exist for the most common failure scenarios
```

**Section score: __ / 8**

---

## Section 5 — Governance & EU AI Act Readiness (10 items)

```
□ The system's risk level has been classified (Annex III check)
□ Technical documentation exists (required by Annex IV for high-risk)
□ Every AI decision is traceable to specific retrieved documents
□ An audit trail is stored and queryable by session/user/date
□ Human oversight mechanism is defined and implemented
□ Data sources used for RAG are documented
□ Data retention policies are defined for audit logs
□ A process exists to handle user requests to explain AI decisions
□ Knowledge base drift is monitored on a schedule
□ An incident response procedure exists for AI failures
```

**Section score: __ / 10**

---

## Total Score

| Section | Score | Max | % |
|---------|-------|-----|---|
| Architecture | | 8 | |
| RAG Quality | | 12 | |
| Evaluation | | 6 | |
| Observability | | 8 | |
| Governance | | 10 | |
| **TOTAL** | | **44** | |

---

## Interpretation

| Score | Status |
|-------|--------|
| ≥ 90% | ✅ **Production Ready** — Your system meets enterprise reliability standards |
| 75–89% | ⚠️ **Conditionally Ready** — Address critical gaps before scaling |
| 60–74% | 🔶 **Pre-production** — Significant work needed before enterprise deployment |
| < 60% | ❌ **Not Ready** — Fundamental reliability concerns must be addressed |

---

## Next Steps

After completing the checklist:

1. **Share your score** — open a [Discussion](https://github.com/raulferrerdev/reliable-enterprise-ai/discussions) to get community input on your gaps
2. **Address critical items first** — Governance gaps are highest risk post-August 2026
3. **Re-assess quarterly** — AI systems degrade; the checklist should be run regularly

---

*Part of the [Reliable Enterprise AI](https://github.com/raulferrerdev/reliable-enterprise-ai) framework by Raúl Ferrer.*  
*Follow on [LinkedIn](https://www.linkedin.com/in/raulferrergarcia) for articles and updates.*
