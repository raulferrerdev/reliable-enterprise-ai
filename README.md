# Reliable Enterprise AI

> *Most enterprise AI systems work in the demo. They fail silently in production.*
> *This repository documents how to build RAG systems that don't.*

**A hands-on knowledge base for building, evaluating and auditing production-grade RAG systems with Java, LangChain4j and Weaviate — from reliability patterns to EU AI Act compliance.**

---

## Who this is for

- Engineers building RAG systems in Java/Spring Boot environments
- Tech leads responsible for AI systems in production
- Teams preparing for EU AI Act compliance
- Anyone who has shipped an AI system and watched it quietly degrade

## The core problem

The gap between a RAG system that *demos well* and one that *works reliably at scale* is where most enterprise AI projects fail. This repository maps that gap — technically, operationally and legally.

---

## Structure

```
reliable-enterprise-ai/
├── modules/
│   ├── 01-rag-foundations/         # Core RAG pipeline: ingest → embed → retrieve → generate
│   ├── 02-advanced-retrieval/      # Hybrid search, re-ranking, HyDE, query expansion
│   ├── 03-reliability-patterns/    # Hallucination detection, confidence scoring, fallbacks
│   ├── 04-evaluation-framework/    # RAGAs metrics, golden datasets, automated evaluation
│   ├── 05-security/                # RAG poisoning, prompt injection, access control
│   └── 06-eu-ai-act/               # Compliance checklist, logging, technical documentation
├── docs/
│   ├── architecture/               # System design decisions and ADRs
│   ├── evaluation/                 # Evaluation methodology and benchmarks
│   ├── compliance/                 # EU AI Act implementation guides
│   └── security/                   # Threat models and mitigation patterns
└── resources/                      # Reading lists, papers, reference implementations
```

---

## The Reliable Enterprise AI Audit Framework

The 5 dimensions every production RAG system must satisfy:

| Dimension | What it measures | Status |
|-----------|-----------------|--------|
| **Performance** | Retrieval precision, latency, throughput at scale | 🔄 In progress |
| **Reliability** | Hallucination rate, confidence calibration, graceful degradation | 🔄 In progress |
| **Security** | Prompt injection resistance, data poisoning, access control | 📋 Planned |
| **Compliance** | EU AI Act requirements, audit trails, documentation | 📋 Planned |
| **Observability** | Tracing, monitoring, alerting for production AI | 📋 Planned |

---

## Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| AI orchestration | LangChain4j |
| Vector database | Weaviate |
| Evaluation | RAGAs (via Python bridge) |
| Observability | OpenTelemetry + LangFuse |
| Testing | JUnit 5 + Testcontainers |

---

## Modules

### 01 · RAG Foundations
*The baseline every enterprise RAG system needs before optimising anything.*

Core pipeline: document ingestion → chunking → embedding → vector storage → retrieval → generation. Built with Spring Boot + LangChain4j + Weaviate. Fully tested, fully observable.

→ [Go to module](modules/01-rag-foundations/README.md)

### 02 · Advanced Retrieval
*Why cosine similarity is not enough for production.*

Hybrid search (dense + sparse), cross-encoder re-ranking, HyDE (Hypothetical Document Embeddings), query expansion strategies. Benchmarked against the baseline from Module 01.

→ [Go to module](modules/02-advanced-retrieval/README.md)

### 03 · Reliability Patterns
*The module most RAG tutorials skip entirely.*

Hallucination detection, confidence scoring, fallback strategies, circuit breakers for LLMs. The patterns that separate a RAG proof-of-concept from a system you can put your name on.

→ [Go to module](modules/03-reliability-patterns/README.md)

### 04 · Evaluation Framework
*You cannot improve what you cannot measure.*

RAGAs metrics (Faithfulness, Answer Relevancy, Context Recall), golden dataset construction, automated regression testing for RAG quality. Reusable across all modules.

→ [Go to module](modules/04-evaluation-framework/README.md)

### 05 · Security
*The attack surface nobody is watching.*

RAG-specific threats: prompt injection, data poisoning, PII leakage, access control in vector databases. Red-teaming methodology for enterprise RAG systems.

→ [Go to module](modules/05-security/README.md)

### 06 · EU AI Act
*Compliance is not a checkbox. It is an architecture decision.*

Technical implementation of EU AI Act requirements for high-risk AI systems: logging, audit trails, technical documentation, conformity assessment. Practical checklist with Java/Spring Boot examples.

→ [Go to module](modules/06-eu-ai-act/README.md)

---

## Progress log

| Week | Milestone | Module |
|------|-----------|--------|
| S1–4 | Java/Spring Boot foundations, project setup | ✅ Setup |
| S5–6 | First embedding pipeline with Weaviate | ✅ 01 |
| S7–8 | LangChain4j integration, first RAG end-to-end | ✅ 01 |
| S9–10 | Full RAG pipeline with basic evaluation | 01 + 04 |
| S11–12 | Baseline benchmarks published | 01 + 04 |
| S13–14 | Chunking strategy comparison | 02 |
| S15–16 | Hybrid search + re-ranking | 02 |
| S17–18 | RAGAs evaluation module | 04 |
| S19–20 | Hallucination detection patterns | 03 |
| ... | ... | ... |

---

## Writing

Articles and posts from this project:

- [Reliable Enterprise AI: Why Architecture Matters More Than Prompts](https://levelup.gitconnected.com/beyond-the-prompt-why-enterprise-ai-needs-architecture-not-just-clever-prompts-3f2523baa47f)
- [Reliable Enterprise AI: What Enterprise Architects Must Understand About Transformers](https://medium.com/gitconnected/reliable-enterprise-ai-attention-is-all-you-need-explained-for-enterprise-architects-bfaebf2c0089)
- [RAG Is Not One Thing: A Practical Architecture Map for Reliable Enterprise AI Systems] (In progress)
---

## About

Built by **[Raúl Ferrer García]** — Tech Lead · PhD · Building Reliable Enterprise AI.

13 years leading engineering teams at Vicens Vives Digital. Author of [*iOS Architecture Patterns* (Apress)](https://www.amazon.com/iOS-Architecture-Patterns-VIPER-Swift/dp/1484290682) . Currently focused on the intersection of production reliability, RAG architecture and EU AI Act compliance.

→ [LinkedIn](https://www.linkedin.com/in/raulferrergarcia/) · [Medium](https://medium.com/@raulferrer)

---

*"You can't lead the reliability of an AI system if you've never built one yourself."*
