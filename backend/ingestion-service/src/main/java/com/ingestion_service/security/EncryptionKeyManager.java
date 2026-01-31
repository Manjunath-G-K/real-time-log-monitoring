package com.ingestion_service.security;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EncryptionKeyManager {

    private String currentKey = generateKey();

    public synchronized String getKey() {
        return currentKey;
    }

    public synchronized void rotateKey() {
        currentKey = generateKey();
    }

    private String generateKey() {
        return UUID.randomUUID().toString();
    }
}
