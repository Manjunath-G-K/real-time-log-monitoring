package com.ingestion_service.controller;

import com.ingestion_service.model.LogRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logs")
public class LogController {

    @PostMapping
    public String receiveLog ( @RequestBody LogRequest logRequest ) {
        System.out.println ("Service: " + logRequest.getService ());
        System.out.println ("Message: " + logRequest.getMessage ());
        return "Log received successfully";
    }
}
