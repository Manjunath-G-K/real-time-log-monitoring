package com.ingestion_service.store;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class InMemoryLogStore {

    private static final int MAX_LOGS = 100;
    private final LinkedList<String> logs = new LinkedList<>();

    public synchronized void addLog(String log) {
        if (logs.size() >= MAX_LOGS) {
            logs.removeFirst();
        }
        logs.addLast(log);
    }

    public synchronized List<String> getRecentLogs() {
        return List.copyOf(logs);
    }
}
