package com.ingestion_service.service;

import com.ingestion_service.model.LogEntry;
import com.ingestion_service.model.LogEntryResponse;
import com.ingestion_service.repository.LogEntryRepository;
import com.ingestion_service.security.CryptoService;
import com.ingestion_service.security.EncryptionKeyManager;
import com.ingestion_service.store.MetricsStore;
import com.ingestion_service.websocket.LogWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

/**
 * Pure unit test: the repository, WebSocket handler, and metrics store are
 * mocked, but EncryptionKeyManager/CryptoService are real instances (they
 * have no external dependencies and are already covered by their own
 * tests) so masking + real encryption still runs end-to-end here.
 */
@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    private LogEntryRepository logEntryRepository;
    @Mock
    private LogWebSocketHandler webSocketHandler;
    @Mock
    private MetricsStore metricsStore;

    private EncryptionKeyManager keyManager;
    private CryptoService cryptoService;
    private LogService logService;

    @BeforeEach
    void setUp() {
        keyManager = new EncryptionKeyManager();
        cryptoService = new CryptoService(keyManager);
        logService = new LogService(logEntryRepository, webSocketHandler, keyManager, cryptoService, metricsStore);

        // Not every test persists a log, so this stub is intentionally lenient.
        lenient().when(logEntryRepository.save(any(LogEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void processLog_withBlankMessage_returnsEmptyAndPersistsNothing() {
        Optional<LogEntryResponse> result = logService.processLog("auth", "INFO", "   ");

        assertThat(result).isEmpty();
        verifyNoInteractions(logEntryRepository, webSocketHandler, metricsStore);
    }

    @Test
    void processLog_masksSensitiveDataBeforeEncrypting() {
        Optional<LogEntryResponse> result =
                logService.processLog("auth", "INFO", "User test@gmail.com called from 9876543210");

        assertThat(result).isPresent();
        String decrypted = cryptoService.decrypt(result.get().ciphertext());
        assertThat(decrypted)
                .contains("***@gmail.com")
                .contains("******3210")
                .doesNotContain("test@gmail.com")
                .doesNotContain("9876543210");
    }

    @Test
    void processLog_defaultsServiceAndLevel_whenNotProvided() {
        Optional<LogEntryResponse> result = logService.processLog(null, null, "no metadata given");

        assertThat(result).isPresent();
        assertThat(result.get().service()).isEqualTo("unknown");
        assertThat(result.get().level()).isEqualTo("INFO");
    }

    @Test
    void processLog_upperCasesProvidedLevel() {
        Optional<LogEntryResponse> result = logService.processLog("auth", "warn", "careful");

        assertThat(result).isPresent();
        assertThat(result.get().level()).isEqualTo("WARN");
    }

    @Test
    void processLog_broadcastsCiphertextAndRecordsMetrics() {
        logService.processLog("auth", "INFO", "hello");

        verify(webSocketHandler).broadcast(anyString());
        verify(metricsStore).recordLog();
    }

    @Test
    void getRecentLogs_withNoServiceFilter_queriesGlobalRecent() {
        when(logEntryRepository.findTop100ByOrderByTimestampDesc()).thenReturn(List.of());

        logService.getRecentLogs(null);

        verify(logEntryRepository).findTop100ByOrderByTimestampDesc();
        verify(logEntryRepository, never()).findTop100ByServiceOrderByTimestampDesc(anyString());
    }

    @Test
    void getRecentLogs_withServiceFilter_queriesByThatServiceOnly() {
        when(logEntryRepository.findTop100ByServiceOrderByTimestampDesc("auth")).thenReturn(List.of());

        logService.getRecentLogs("auth");

        verify(logEntryRepository).findTop100ByServiceOrderByTimestampDesc("auth");
        verify(logEntryRepository, never()).findTop100ByOrderByTimestampDesc();
    }

    @Test
    void panic_rotatesKeyPurgesLogsResetsMetricsAndBroadcastsEmergencyMessage() {
        int versionBeforePanic = keyManager.getCurrentVersion();
        when(logEntryRepository.count()).thenReturn(5L);

        logService.panic();

        assertThat(keyManager.getCurrentVersion()).isGreaterThan(versionBeforePanic);
        verify(logEntryRepository).deleteAllInBatch();
        verify(metricsStore).reset();
        verify(webSocketHandler).broadcast(contains("PANIC MODE"));
    }
}
