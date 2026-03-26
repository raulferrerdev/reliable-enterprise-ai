package com.phoneix.ragfoundation.retrieval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class RetrievalAuditLog {

    private static final Logger log =
            LoggerFactory.getLogger(RetrievalAuditLog.class);

    public record RetrievalEvent(
            String queryId,
            String question,
            int chunksRetrieved,
            boolean contextSufficient,
            String answer,
            Instant timestamp
    ) {}

    public void log(RetrievalEvent event) {
        log.info("""
            [RAG-AUDIT] \
            queryId={} | \
            chunks={} | \
            contextFound={} | \
            question="{}" | \
            answer="{}" | \
            ts={}
            """,
                event.queryId(),
                event.chunksRetrieved(),
                event.contextSufficient(),
                event.question(),
                event.answer().substring(0, Math.min(80, event.answer().length())),
                event.timestamp()
        );
    }
}