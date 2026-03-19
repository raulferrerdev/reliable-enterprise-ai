# EU AI Act Compliance Checklist for RAG Systems

> Practical checklist for engineering teams auditing enterprise RAG deployments.
> Based on Regulation (EU) 2024/1689 requirements for high-risk AI systems.

**Version:** 0.1 (draft)
**Last updated:** 2025
**Status:** Work in progress — updated as Module 06 develops

---

## How to use this checklist

Each item maps to a specific EU AI Act article. For each item:
- ✅ Compliant — requirement met, evidence documented
- 🔄 In progress — partially implemented
- ❌ Gap — requirement not yet addressed
- N/A — not applicable to this deployment

---

## 1 · Risk classification (Art. 6)

- [ ] Determined whether the system falls under Annex III high-risk categories
- [ ] Documented the risk classification decision with justification
- [ ] Assessed whether GPAI (general-purpose AI) provisions apply

---

## 2 · Technical documentation (Art. 11 + Annex IV)

- [ ] General description of the AI system and its intended purpose
- [ ] Description of the system architecture (retrieval pipeline, models used)
- [ ] Training data description (if applicable) or model provenance documentation
- [ ] Description of the chunking and indexing methodology
- [ ] Embedding model documentation (provider, version, known limitations)
- [ ] LLM documentation (provider, version, known limitations)
- [ ] Description of the evaluation methodology and results
- [ ] Known limitations and foreseeable misuse cases documented

---

## 3 · Record-keeping and logging (Art. 12)

- [ ] Automatic logging of all queries and responses enabled
- [ ] Logs include: timestamp, user ID (anonymised), query, retrieved context, response, model version
- [ ] Log retention policy defined and implemented (minimum as per applicable law)
- [ ] Logs are tamper-evident
- [ ] Log access is restricted and auditable

---

## 4 · Transparency to users (Art. 13)

- [ ] Users informed they are interacting with an AI system
- [ ] Confidence levels or uncertainty communicated where relevant
- [ ] System limitations disclosed to users
- [ ] Contact point for human review provided

---

## 5 · Human oversight (Art. 14)

- [ ] Human override mechanism implemented
- [ ] Escalation path defined for low-confidence responses
- [ ] Monitoring interface available for human supervisors
- [ ] Training provided to human overseers

---

## 6 · Accuracy, robustness and cybersecurity (Art. 15)

- [ ] Evaluation framework in place (→ see Module 04)
- [ ] Hallucination detection implemented (→ see Module 03)
- [ ] Adversarial input testing conducted (→ see Module 05)
- [ ] Performance monitoring in production enabled
- [ ] Regression testing on model/data updates

---

## 7 · Conformity assessment

- [ ] Internal conformity assessment completed (for most high-risk systems)
- [ ] CE marking applied (where required)
- [ ] Declaration of conformity prepared

---

*This checklist is maintained as part of the [Reliable Enterprise AI](../../README.md) project.*
*Not legal advice. Consult qualified legal counsel for compliance decisions.*
