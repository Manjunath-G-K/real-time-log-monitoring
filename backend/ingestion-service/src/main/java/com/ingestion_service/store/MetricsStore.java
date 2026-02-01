package com.ingestion_service.store;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MetricsStore {

    private final AtomicInteger totalLogs = new AtomicInteger(0);
    private volatile Instant lastLogTime;

    public void recordLog() {
        totalLogs.incrementAndGet();
        lastLogTime = Instant.now();
    }

    public int getTotalLogs() {
        return totalLogs.get();
    }

    public Instant getLastLogTime() {
        return lastLogTime;
    }

    public void reset() {
        totalLogs.set(0);
        lastLogTime = null;
    }
}
