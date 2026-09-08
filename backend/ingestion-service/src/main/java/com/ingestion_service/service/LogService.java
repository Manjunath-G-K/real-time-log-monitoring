package com.ingestion_service.service;

import com.ingestion_service.model.LogEntry;
import com.ingestion_service.model.LogEntryResponse;
import com.ingestion_service.repository.LogEntryRepository;
import com.ingestion_service.security.CryptoService;
import com.ingestion_service.security.EncryptionKeyManager;
import com.ingestion_service.store.MetricsStore;
import com.ingestion_service.websocket.LogWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LogService {

    private static final Logger log = LoggerFactory.getLogger(LogService.class);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\b\\d{10}\\b");

    private static final String DEFAULT_SERVICE = "unknown";
    private static final String DEFAULT_LEVEL = "INFO";

    private final LogEntryRepository logEntryRepository;

    private final LogWebSocketHandler webSocketHandler;

    private final EncryptionKeyManager keyManager;

    private final CryptoService cryptoService;

    private final MetricsStore metricsStore;


    public LogService(LogEntryRepository logEntryRepository,
                      LogWebSocketHandler webSocketHandler,
                      EncryptionKeyManager keyManager,
                      CryptoService cryptoService,
                      MetricsStore metricsStore) {
        this.logEntryRepository = logEntryRepository;
        this.webSocketHandler = webSocketHandler;
        this.keyManager = keyManager;
        this.cryptoService = cryptoService;
        this.metricsStore = metricsStore;
    }




    /**
     * Returns empty when the message is blank — the caller (LogController)
     * decides what HTTP status that becomes. Nothing is persisted in that
     * case.
     */
    @Transactional
    public Optional<LogEntryResponse> processLog(String service, String level, String message) {

        if (message == null || message.isBlank()) {
            log.debug("Ignored empty log from service={}", service);
            return Optional.empty();
        }

        String resolvedService = (service == null || service.isBlank()) ? DEFAULT_SERVICE : service;
        String resolvedLevel = (level == null || level.isBlank()) ? DEFAULT_LEVEL : level.toUpperCase();

        String maskedMessage = maskSensitiveData(message);
        String encryptedMessage = cryptoService.encrypt(maskedMessage);

        LogEntry saved = logEntryRepository.save(
                new LogEntry(resolvedService, resolvedLevel, Instant.now(), encryptedMessage));

        webSocketHandler.broadcast(encryptedMessage);

        metricsStore.recordLog();

        // Deliberately never log the masked/plaintext message or the
        // ciphertext itself here — that would defeat the point of masking
        // and encrypting it in the first place. Only log metadata useful
        // for tracing an ingestion problem.
        log.info("Processed log: id={}, service={}, level={}, keyVersion={}",
                saved.getId(), resolvedService, resolvedLevel, keyManager.getCurrentVersion());

        return Optional.of(LogEntryResponse.from(saved));
    }

    /**
     * Recent logs, most recent first, optionally filtered to one service.
     * "Recent" is a fixed view-window (see LogEntryRepository) — it does
     * not delete anything older, it just doesn't return it here.
     */
    public List<LogEntryResponse> getRecentLogs(String service) {
        List<LogEntry> entries = (service == null || service.isBlank())
                ? logEntryRepository.findTop100ByOrderByTimestampDesc()
                : logEntryRepository.findTop100ByServiceOrderByTimestampDesc(service);

        return entries.stream().map(LogEntryResponse::from).toList();
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

    /**
     * Decrypts a previously encrypted, stored log entry.
     *
     * Not yet wired to any REST endpoint on purpose: until there's an
     * auth boundary (step 5 on the roadmap) distinguishing viewers from
     * anonymous callers, exposing "decrypt everything" over HTTP would
     * defeat the purpose of encrypting logs at all. This method exists
     * now so it can be unit-tested and then wired in once auth exists.
     */
    public String decrypt(String encryptedMessage) {
        return cryptoService.decrypt(encryptedMessage);
    }


    @Transactional
    public void panic() {
        long purgedCount = logEntryRepository.count();

        keyManager.rotateKey();
        logEntryRepository.deleteAllInBatch();
        metricsStore.reset();
        webSocketHandler.broadcast("🚨 PANIC MODE ACTIVATED: Logs invalidated");

        log.warn("Panic mode activated: encryption key rotated, {} log(s) purged, metrics reset", purgedCount);
    }

}
