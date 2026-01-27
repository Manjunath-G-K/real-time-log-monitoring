package com.ingestion_service.service;

import org.springframework.stereotype.Service;

@Service
public class LogService {

    public String processLog(String service, String message) {
        String encryptedMessage = encrypt(message);

        System.out.println("Service: " + service);
        System.out.println("Encrypted Message: " + encryptedMessage);

        return encryptedMessage;
    }

    private String encrypt(String message) {
        // Simple encryption for learning (Base64  )
        return java.util.Base64.getEncoder().encodeToString(message.getBytes());
    }
}
