# Module 03 · Reliability Patterns

> The module most RAG tutorials skip entirely.

## Patterns implemented

- **Hallucination detection** — grounding checks against retrieved context
- **Confidence scoring** — quantifying uncertainty in LLM responses
- **Fallback strategies** — graceful degradation when retrieval quality drops
- **Circuit breakers for LLMs** — preventing cascading failures in production
- **Response validation** — schema and constraint enforcement on LLM output

## The reliability gap

A RAG system that answers confidently when it shouldn't is more dangerous than one that says "I don't know."
This module builds the scaffolding for systems that fail safely.

## Status: 📋 Planned — S19–22
