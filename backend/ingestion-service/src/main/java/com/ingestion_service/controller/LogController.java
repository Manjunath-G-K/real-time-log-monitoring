

package com.ingestion_service.controller;

import com.ingestion_service.model.LogRequest;
import com.ingestion_service.service.LogService;
import com.ingestion_service.store.MetricsStore;
import org.springframework.web.bind.annotation.*;

import com.ingestion_service.store.InMemoryLogStore;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    private final InMemoryLogStore logStore;
    private final MetricsStore metricsStore;

    public LogController(LogService logService,
                         InMemoryLogStore logStore,
                         MetricsStore metricsStore) {
        this.logService = logService;
        this.logStore = logStore;
        this.metricsStore = metricsStore;
    }



    @PostMapping
    public String receiveLog(@RequestBody LogRequest logRequest) {
        logService.processLog(
                logRequest.getService(),
                logRequest.getMessage()
        );
        return "Log received and processed";
    }

    @GetMapping
    public List<String> getRecentLogs() {
        return logStore.getRecentLogs();
    }

    @PostMapping("/panic")
    public String panicMode() {
        logService.panic();
        return "PANIC MODE ACTIVATED";
    }

    @GetMapping("/health")
    public String health(){
        return "OK" ;
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return Map.of(
                "totalLogs", metricsStore.getTotalLogs(),
                "lastLogTime", metricsStore.getLastLogTime()
        );
    }



}
