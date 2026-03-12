# Governance Patterns — Reliable Enterprise AI

> Auditability, compliance and drift detection for AI systems in regulated industries.

---

## The Governance Imperative

```
BEFORE EU AI Act:              AFTER EU AI Act (Aug 2026):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"It works, ship it."           "Can you prove it worked correctly?"
No audit required.             Full traceability required (Annex IV).
No risk classification.        Risk classification mandatory (Annex III).
No documentation standard.     Technical documentation required.
No fine for failures.          Up to €35M or 7% global turnover.
```

**Governance is not optional — it's architecture.**

---

## Audit Trail Pattern

Every decision made by an AI system must be explainable and reproducible.

```java
@Entity
@Table(name = "ai_audit_events")
public class AuditEvent {

    @Id
    private String eventId;

    private String sessionId;
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String query;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(columnDefinition = "JSONB")
    private String retrievedDocuments;   // Source, chunk, score

    @Column(columnDefinition = "TEXT")
    private String promptTemplate;       // Exact prompt version used

    private String modelId;              // Exact model version
    private String modelVersion;

    private double faithfulnessScore;
    private double answerRelevancyScore;

    private Instant timestamp;
    private String ipAddress;
    private String requestId;
}
```

```java
@Component
public class AuditTrailAdapter implements AuditPort {

    private final AuditEventRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void record(AuditEvent event) {
        // Every AI interaction is persisted
        // This is your compliance record
        repository.save(event);
    }

    @Override
    public List<AuditEvent> getTrace(String sessionId) {
        // Reconstruct the full reasoning chain for any session
        return repository.findBySessionIdOrderByTimestamp(sessionId);
    }

    /**
     * Generate a compliance report for a specific time range.
     * Required for EU AI Act Annex IV documentation.
     */
    public ComplianceReport generateReport(LocalDate from, LocalDate to) {
        List<AuditEvent> events = repository.findByTimestampBetween(
            from.atStartOfDay().toInstant(ZoneOffset.UTC),
            to.atTime(23, 59, 59).toInstant(ZoneOffset.UTC)
        );

        return ComplianceReport.builder()
            .period(from + " to " + to)
            .totalInteractions(events.size())
            .averageFaithfulness(average(events, AuditEvent::getFaithfulnessScore))
            .hallucinationRate(hallucinationRate(events))
            .uniqueUsers(countUniqueUsers(events))
            .build();
    }
}
```

---

## EU AI Act Risk Classification — `risk-classification/`

Determine which category your AI system falls into before regulators do.

```java
public enum AIActRiskLevel {
    UNACCEPTABLE,   // Prohibited — Art. 5
    HIGH,           // Full compliance required — Annex III
    LIMITED,        // Transparency obligations — Art. 50
    MINIMAL         // No specific obligations
}

@Service
public class RiskClassificationService {

    /**
     * Annex III risk categories that require full compliance:
     * - Biometric identification
     * - Critical infrastructure management
     * - Education and vocational training
     * - Employment and HR management
     * - Access to essential private services
     * - Law enforcement
     * - Migration and border control
     * - Administration of justice
     */
    public RiskAssessment classify(AISystemDescriptor system) {

        if (isProhibited(system)) {
            return RiskAssessment.of(AIActRiskLevel.UNACCEPTABLE, "System is prohibited under Art. 5");
        }

        if (isHighRisk(system)) {
            return RiskAssessment.builder()
                .level(AIActRiskLevel.HIGH)
                .requiredActions(List.of(
                    "Implement risk management system (Art. 9)",
                    "Ensure data governance (Art. 10)",
                    "Create technical documentation (Art. 11, Annex IV)",
                    "Enable human oversight (Art. 14)",
                    "Achieve accuracy and robustness (Art. 15)",
                    "Register in EU database (Art. 49)"
                ))
                .deadline("August 2, 2026")
                .build();
        }

        // ... LIMITED and MINIMAL classification
        return RiskAssessment.of(AIActRiskLevel.MINIMAL, "Standard good practices apply");
    }
}
```

---

## Knowledge Base Drift Detection — `drift-detection/`

Vector databases don't age gracefully. Documents become stale, embeddings drift from current language, and your RAG system degrades silently.

```java
@Component
@Scheduled(cron = "0 0 2 * * MON")   // Every Monday at 2am
public class KnowledgeBaseDriftDetector {

    private final VectorStorePort vectorStore;
    private final EvaluationPort evaluator;
    private final AlertPort alerts;

    private static final double DRIFT_THRESHOLD = 0.05;  // 5% degradation triggers alert

    public DriftReport detectDrift() {

        // Use a fixed set of golden queries with known expected quality
        List<EvalCase> goldenCases = loadGoldenDataset();

        double currentScore = evaluator.evaluateBatch(goldenCases).getAverageScore();
        double baselineScore = loadBaselineScore();

        double drift = baselineScore - currentScore;

        DriftReport report = DriftReport.builder()
            .checkDate(LocalDate.now())
            .baselineScore(baselineScore)
            .currentScore(currentScore)
            .drift(drift)
            .driftDetected(drift > DRIFT_THRESHOLD)
            .build();

        if (report.isDriftDetected()) {
            alerts.send(Alert.critical(
                "Knowledge base drift detected",
                "Quality degraded by %.1f%%. Re-indexing may be required.".formatted(drift * 100)
            ));
        }

        return report;
    }
}
```

---

## Compliance Checklist — EU AI Act Annex III Systems

```
PRE-DEPLOYMENT:
□ Risk classification documented
□ Technical documentation complete (Annex IV)
□ Training data documented and validated
□ Human oversight mechanism defined
□ Accuracy benchmarks established and met
□ Registered in EU AI database (if required)

PRODUCTION:
□ Every inference logged with full context
□ Audit trail queryable by regulators
□ Evaluation scores stored per interaction
□ Drift detection running weekly
□ Incident response procedure documented

ONGOING:
□ Monthly compliance report generated
□ Annual technical documentation review
□ Re-validation after model updates
□ Data retention policy enforced
```

---

## Governance Architecture Summary

```
EVERY AI INTERACTION:
  Request ──► [AI Pipeline] ──► Response
                   │
                   ▼
            [Audit Trail]
                   │
        ┌──────────┼──────────┐
        │          │          │
   [Compliance] [Drift     [Quality
   [Report]     Detection] [Dashboard]
        │          │          │
   [Regulator] [Alert    [Engineering
   [Export]    [System]  [Team]
```

> **The question is not whether you will be audited.  
> The question is whether you will be ready when you are.**
