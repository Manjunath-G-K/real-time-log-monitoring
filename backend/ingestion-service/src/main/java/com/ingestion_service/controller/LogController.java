package com.ingestion_service.controller;

import com.ingestion_service.model.LogEntryResponse;
import com.ingestion_service.model.LogRequest;
import com.ingestion_service.service.LogService;
import com.ingestion_service.store.MetricsStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;
    private final MetricsStore metricsStore;

    public LogController(LogService logService, MetricsStore metricsStore) {
        this.logService = logService;
        this.metricsStore = metricsStore;
    }



    @PostMapping
    public ResponseEntity<?> receiveLog(@RequestBody LogRequest logRequest) {
        Optional<LogEntryResponse> saved = logService.processLog(
                logRequest.getService(),
                logRequest.getLevel(),
                logRequest.getMessage()
        );

        return saved
                .<ResponseEntity<?>>map(entry -> ResponseEntity.status(HttpStatus.CREATED).body(entry))
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "message must not be blank")));
    }

    @GetMapping
    public List<LogEntryResponse> getRecentLogs(@RequestParam(required = false) String service) {
        return logService.getRecentLogs(service);
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
        Map<String, Object> response = new HashMap<>();
        response.put("totalLogs", metricsStore.getTotalLogs());
        response.put("lastLogTime", metricsStore.getLastLogTime());
        return response;
    }

}
