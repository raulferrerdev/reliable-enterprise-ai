# Reliable Enterprise AI

> **LEarning to design AI systems that enterprises can trust in production.**
> Architecture · Evaluation · Governance

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://openjdk.org/)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-latest-green)](https://github.com/langchain4j/langchain4j)
[![Weaviate](https://img.shields.io/badge/Weaviate-vector--db-orange)](https://weaviate.io/)

---

## What is Reliable Enterprise AI?

Most enterprise AI projects fail **not because of models**, but because of gaps in **architecture, evaluation, observability, and governance**.

**Reliable Enterprise AI (RAI)** is a framework for building production-grade AI systems with:

* 🏗️ **Architecture** that scales beyond the demo
* 📊 **Evaluation** that measures what actually matters
* 🔒 **Governance** that satisfies regulators and stakeholders
* 👁️ **Observability** for continuous reliability monitoring

This repository is the **technical companion** to the articles and framework developed by [Raúl Ferrer](https://www.linkedin.com/in/raulferrergarcia/) on production AI systems.

---

## The 5 Layers Framework

```
┌─────────────────────────────────────────┐
│              USER REQUEST               │
└────────────────────┬────────────────────┘
                     │
┌────────────────────▼────────────────────┐
│           APPLICATION LAYER             │
│  Orchestration · API · Session mgmt     │
└────────────────────┬────────────────────┘
                     │
┌────────────────────▼────────────────────┐
│             CONTEXT LAYER               │
│  RAG · Retrieval · Re-ranking · Memory  │
└────────────────────┬────────────────────┘
                     │
┌────────────────────▼────────────────────┐
│              MODEL LAYER                │
│  LLM · Prompts · Fine-tuning · Routing  │
└────────────────────┬────────────────────┘
                     │
┌────────────────────▼────────────────────┐
│           EVALUATION LAYER              │
│  Metrics · Benchmarks · Quality Gates   │
└────────────────────┬────────────────────┘
                     │
┌────────────────────▼────────────────────┐
│           GOVERNANCE LAYER              │
│  Audit · Compliance · EU AI Act · Drift │
└────────────────────┬────────────────────┘
                     │
┌────────────────────▼────────────────────┐
│              MONITORING                 │
│  Observability · Alerts · Dashboards    │
└─────────────────────────────────────────┘
```

---

## Repository Structure

```
reliable-enterprise-ai/
│
├── architecture/              # Hexagonal & layered AI system design
│   ├── README.md
│   ├── hexagonal-ai-system/   # Full hexagonal architecture example
│   └── patterns/              # Reusable architectural patterns
│
├── rag-patterns/              # Production RAG implementations
│   ├── README.md
│   ├── basic-rag/             # LangChain4j + Weaviate baseline
│   ├── hybrid-search/         # Dense + sparse retrieval
│   ├── reranking/             # Cross-encoder re-ranking pipeline
│   └── context-compression/   # Reducing context window noise
│
├── evaluation-pipelines/      # LLM evaluation frameworks
│   ├── README.md
│   ├── metrics/               # Faithfulness, relevancy, precision
│   ├── quality-gates/         # CI/CD integration for LLM quality
│   └── hallucination-detection/ # Experiment + benchmark
│
├── governance-patterns/       # EU AI Act & compliance patterns
│   ├── README.md
│   ├── audit-trail/           # Traceability implementation
│   ├── risk-classification/   # Annex III risk assessment templates
│   └── drift-detection/       # Knowledge base decay monitoring
│
├── diagrams/                  # Architecture diagrams (Mermaid, PNG/SVG)
│   └── README.md
│
├── checklist/                 # Reliable Enterprise AI Checklist
│   └── RELIABLE-ENTERPRISE-AI-CHECKLIST.md
│
└── .github/
    ├── CONTRIBUTING.md
    └── DISCUSSION_TEMPLATE.md
```

---

## Tech Stack

| Layer            | Technology       |
| ---------------- | ---------------- |
| Language         | Java 17+         |
| AI Orchestration | LangChain4j      |
| Vector Database  | Weaviate         |
| LLM Evaluation   | RAGAS · DeepEval |
| Observability    | Langfuse         |
| Build            | Maven / Gradle   |

---

## Articles & Resources

> Each folder in this repo is the technical companion to a published article.

| Article                                                                | Folder                  | Status    |
| ---------------------------------------------------------------------- | ----------------------- | --------- |
| [Reliable Enterprise AI: Why Architecture Matters More Than Models](#) | `architecture/`         | 🔜 Coming |
| [Why Most RAG Systems Fail in Production](#)                           | `rag-patterns/`         | 🔜 Coming |
| [The Architecture of Reliable Enterprise AI Systems](#)                | `architecture/`         | 🔜 Coming |
| [Evaluation: The Missing Layer in Enterprise AI](#)                    | `evaluation-pipelines/` | 🔜 Coming |
| [AI Governance for Reliable Enterprise AI](#)                          | `governance-patterns/`  | 🔜 Coming |

---

## Quickstart

```bash
git clone https://github.com/raulferrerdev/reliable-enterprise-ai.git
cd reliable-enterprise-ai

# Run the basic RAG demo (Java 17+, Docker required for Weaviate)
cd rag-patterns/basic-rag
./mvnw spring-boot:run
```

> Full setup instructions are in each module's README.

---

## Author

**Raúl Ferrer** — AI Systems Reliability Architect
*Reliable Enterprise AI | RAG Architecture | LLM Evaluation*

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue)](https://www.linkedin.com/in/raulferrergarcia/)
[![Medium](https://img.shields.io/badge/Medium-Articles-black)](https://medium.com/@raulferrer)

---

## License

MIT © Raúl Ferrer

