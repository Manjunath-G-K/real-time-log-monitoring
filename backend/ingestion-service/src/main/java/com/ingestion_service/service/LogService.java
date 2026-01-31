package com.ingestion_service.service;

import com.ingestion_service.websocket.LogWebSocketHandler;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ingestion_service.store.InMemoryLogStore;
import com.ingestion_service.security.EncryptionKeyManager;



@Service
public class LogService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\b\\d{10}\\b");

    private final InMemoryLogStore logStore;

    private final LogWebSocketHandler webSocketHandler;





    private final EncryptionKeyManager keyManager;

    public LogService(InMemoryLogStore logStore,
                      LogWebSocketHandler webSocketHandler,
                      EncryptionKeyManager keyManager) {
        this.logStore = logStore;
        this.webSocketHandler = webSocketHandler;
        this.keyManager = keyManager;
    }




    public String processLog(String service, String message) {
        String maskedMessage = maskSensitiveData(message);
        String encryptedMessage = encrypt(maskedMessage);

        logStore.addLog(encryptedMessage);

        webSocketHandler.broadcast(encryptedMessage);

        System.out.println("Service: " + service);
        System.out.println("Encrypted Message: " + encryptedMessage);

        return encryptedMessage;
    }

    private String maskSensitiveData(String message) {
        message = maskEmail(message);
        message = maskPhone(message);
        return message;
    }

    private String maskEmail(String message) {
        Matcher matcher = EMAIL_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String email = matcher.group();
            String maskedEmail = "***@" + email.substring(email.indexOf("@") + 1);
            matcher.appendReplacement(result, maskedEmail);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String maskPhone(String message) {
        Matcher matcher = PHONE_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String phone = matcher.group();
            String maskedPhone = "******" + phone.substring(6);
            matcher.appendReplacement(result, maskedPhone);
        }
        matcher.appendTail(result);
        return result.toString();
    }

//    private String encrypt(String message) {
//        return Base64.getEncoder().encodeToString(message.getBytes());
//    }

    private String encrypt(String message) {
        String key = keyManager.getKey();
        String combined = key + ":" + message;
        return Base64.getEncoder().encodeToString(combined.getBytes());
    }


    public void panic() {
        keyManager.rotateKey();
        logStore.clear();
        webSocketHandler.broadcast("🚨 PANIC MODE ACTIVATED: Logs invalidated");
    }



}
