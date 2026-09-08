package com.ingestion_service.repository;

import com.ingestion_service.model.LogEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Derived query method names (findTop100By...OrderByTimestampDesc) are
 * easy to get subtly wrong — wrong field name, wrong direction, wrong
 * limit — and Spring Data won't catch that at compile time, only at
 * runtime against a real database. This boots the full context (this
 * project's custom "webmvc-test" starter doesn't pull in
 * spring-boot-test-autoconfigure, so @DataJpaTest isn't available here)
 * against the in-memory H2 configured in test/resources/application.
 * properties. @Transactional rolls each test back, so tests don't leak
 * data into one another.
 */
@SpringBootTest
@Transactional
class LogEntryRepositoryTest {

    @Autowired
    private LogEntryRepository repository;

    @Test
    void findTop100ByOrderByTimestampDesc_returnsMostRecentFirst() {
        Instant now = Instant.now();
        repository.save(new LogEntry("auth", "INFO", now.minus(2, ChronoUnit.MINUTES), "old"));
        repository.save(new LogEntry("auth", "INFO", now, "newest"));
        repository.save(new LogEntry("billing", "INFO", now.minus(1, ChronoUnit.MINUTES), "middle"));

        List<LogEntry> results = repository.findTop100ByOrderByTimestampDesc();

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getCiphertext()).isEqualTo("newest");
        assertThat(results.get(2).getCiphertext()).isEqualTo("old");
    }

    @Test
    void findTop100ByServiceOrderByTimestampDesc_filtersToOnlyThatService() {
        Instant now = Instant.now();
        repository.save(new LogEntry("auth", "INFO", now, "auth-log"));
        repository.save(new LogEntry("billing", "INFO", now, "billing-log"));

        List<LogEntry> results = repository.findTop100ByServiceOrderByTimestampDesc("billing");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getService()).isEqualTo("billing");
    }
}
