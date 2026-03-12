# Diagrams — Reliable Enterprise AI

> All architecture diagrams in Mermaid format. GitHub renders these natively.

---

## 1. The 5 Layers Framework

```mermaid
graph TD
    U[👤 User Request] --> A
    A[Application Layer<br/>Orchestration · API · Session] --> C
    C[Context Layer<br/>RAG · Retrieval · Re-ranking · Memory] --> M
    M[Model Layer<br/>LLM · Prompts · Routing] --> E
    E[Evaluation Layer<br/>Metrics · Quality Gates · Benchmarks] --> G
    G[Governance Layer<br/>Audit · Compliance · EU AI Act] --> MON
    MON[📊 Monitoring<br/>Observability · Alerts · Dashboards]

    style U fill:#4A90D9,color:#fff
    style A fill:#5BA85A,color:#fff
    style C fill:#5BA85A,color:#fff
    style M fill:#5BA85A,color:#fff
    style E fill:#E8973A,color:#fff
    style G fill:#D9534F,color:#fff
    style MON fill:#7B68EE,color:#fff
```

---

## 2. Reliable RAG Pipeline

```mermaid
graph TD
    Q[User Query] --> QP[Query Processor<br/>Expansion · Intent · Routing]
    QP --> RET[Retriever<br/>Dense + Sparse Hybrid Search]
    RET --> WEA[(Weaviate<br/>Vector Store)]
    WEA --> RET
    RET --> RR[Re-Ranker<br/>Cross-encoder Scoring]
    RR --> CB[Context Builder<br/>Token Budget · Compression]
    CB --> LLM[LLM<br/>via LangChain4j]
    LLM --> EV[Evaluation<br/>Faithfulness · Relevancy]
    EV --> AT[Audit Trail<br/>Full Traceability]
    EV --> RESP[✅ Response + Sources]

    style Q fill:#4A90D9,color:#fff
    style WEA fill:#FF8C00,color:#fff
    style LLM fill:#6A5ACD,color:#fff
    style EV fill:#E8973A,color:#fff
    style AT fill:#D9534F,color:#fff
    style RESP fill:#5BA85A,color:#fff
```

---

## 3. Hexagonal Architecture for AI Systems

```mermaid
graph LR
    subgraph DRIVING["Driving Adapters (Input)"]
        REST[REST API]
        CLI[CLI]
        BATCH[Batch Job]
    end

    subgraph DOMAIN["Domain Core"]
        QS[AIQueryService]
        IS[IngestionService]
        ES[EvaluationService]

        subgraph PORTS["Ports (Interfaces)"]
            LP[LLMPort]
            VP[VectorStorePort]
            EP[EvaluationPort]
            AP[AuditPort]
        end
    end

    subgraph DRIVEN["Driven Adapters (Output)"]
        LC[LangChain4j<br/>Adapter]
        WV[Weaviate<br/>Adapter]
        LF[Langfuse<br/>Adapter]
        DB[(Audit DB)]
    end

    REST --> QS
    CLI --> IS
    BATCH --> ES

    QS --> LP
    QS --> VP
    QS --> AP
    IS --> VP
    ES --> EP

    LP --> LC
    VP --> WV
    EP --> LF
    AP --> DB

    style DOMAIN fill:#f0f4ff,stroke:#4A90D9
    style DRIVING fill:#f0fff0,stroke:#5BA85A
    style DRIVEN fill:#fff8f0,stroke:#E8973A
```

---

## 4. LLM Evaluation Pipeline

```mermaid
graph TD
    DS[📋 Eval Dataset<br/>query · expected · context] --> RAG[RAG Pipeline]
    RAG --> TR[query · answer · context triplet]
    TR --> FE[Faithfulness<br/>Evaluator]
    TR --> RE[Answer Relevancy<br/>Evaluator]
    TR --> CE[Context Precision<br/>Evaluator]

    FE --> QG{Quality Gate}
    RE --> QG
    CE --> QG

    QG -->|PASS ✅| DEP[Deploy]
    QG -->|FAIL ❌| BLOCK[Block + Alert]

    style DS fill:#4A90D9,color:#fff
    style QG fill:#E8973A,color:#fff
    style DEP fill:#5BA85A,color:#fff
    style BLOCK fill:#D9534F,color:#fff
```

---

## 5. Knowledge Base Drift Over Time

```mermaid
xychart-beta
    title "RAG Quality Score Over Time (example)"
    x-axis ["Jan", "Feb", "Mar", "Apr", "May", "Jun"]
    y-axis "Faithfulness Score" 0.5 --> 1.0
    line [0.91, 0.89, 0.87, 0.82, 0.76, 0.71]
```

---

## 6. EU AI Act Compliance Flow

```mermaid
graph TD
    START[New AI System] --> CLASS{Risk Classification}

    CLASS -->|Art. 5| PROHIB[🚫 Prohibited<br/>Cannot deploy]
    CLASS -->|Annex III| HIGH[⚠️ High Risk<br/>Full compliance required]
    CLASS -->|Art. 50| LIMITED[ℹ️ Limited Risk<br/>Transparency obligations]
    CLASS -->|Other| MINIMAL[✅ Minimal Risk<br/>Good practices apply]

    HIGH --> DOC[Technical Documentation<br/>Annex IV]
    HIGH --> HO[Human Oversight<br/>Art. 14]
    HIGH --> AUDIT[Audit Trail<br/>Art. 12]
    HIGH --> REG[EU Database Registration<br/>Art. 49]

    DOC --> DEPLOY[✅ Deploy]
    HO --> DEPLOY
    AUDIT --> DEPLOY
    REG --> DEPLOY

    style PROHIB fill:#D9534F,color:#fff
    style HIGH fill:#E8973A,color:#fff
    style LIMITED fill:#4A90D9,color:#fff
    style MINIMAL fill:#5BA85A,color:#fff
    style DEPLOY fill:#5BA85A,color:#fff
```

---

## How to Use These Diagrams

All diagrams are in **Mermaid format** and render automatically on GitHub.

To use in your articles or presentations:
1. Copy the Mermaid code block
2. Paste into any Mermaid-compatible editor (mermaid.live)
3. Export as SVG or PNG for use in Medium, LinkedIn, or slides

> **These diagrams are the visual identity of Reliable Enterprise AI.**  
> Use them consistently across all content.
