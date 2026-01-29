

package com.ingestion_service.controller;

import com.ingestion_service.model.LogRequest;
import com.ingestion_service.service.LogService;
import org.springframework.web.bind.annotation.*;

import com.ingestion_service.store.InMemoryLogStore;
import java.util.List;


@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    private final InMemoryLogStore logStore;

    public LogController(LogService logService, InMemoryLogStore logStore) {
        this.logService = logService;
        this.logStore = logStore;
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

}
