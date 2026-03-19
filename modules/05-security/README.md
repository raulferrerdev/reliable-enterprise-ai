# Module 05 · Security

> The attack surface nobody is watching.

## Threat model

| Threat | Severity |
|--------|----------|
| Prompt injection via documents | Critical |
| Data poisoning in vector store | High |
| PII leakage through retrieval | High |
| Access control bypass | High |
| Embedding inversion | Medium |

## Mitigations implemented

- Input sanitisation before embedding
- PII detection and redaction pipeline
- Role-based access control in Weaviate
- Red-teaming checklist for enterprise RAG

## Status: 📋 Planned — S34–36
