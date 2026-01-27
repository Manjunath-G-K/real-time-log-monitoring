//package com.ingestion_service.controller;
//
//import com.ingestion_service.model.LogRequest;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/logs")
//public class LogController {
//
//    @PostMapping
//    public String receiveLog ( @RequestBody LogRequest logRequest ) {
//        System.out.println ("Service: " + logRequest.getService ());
//        System.out.println ("Message: " + logRequest.getMessage ());
//        return "Log received successfully";
//    }
//}


package com.ingestion_service.controller;

import com.ingestion_service.model.LogRequest;
import com.ingestion_service.service.LogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping
    public String receiveLog(@RequestBody LogRequest logRequest) {
        logService.processLog(
                logRequest.getService(),
                logRequest.getMessage()
        );
        return "Log received and processed";
    }
}
