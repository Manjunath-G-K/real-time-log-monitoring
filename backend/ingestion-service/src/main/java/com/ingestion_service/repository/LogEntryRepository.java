package com.ingestion_service.repository;

import com.ingestion_service.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link LogEntry}.
 *
 * Responsibility: translate two query shapes ("recent logs", "recent logs
 * for one service") into SQL. Spring Data generates the implementation
 * from the method name at startup — no hand-written JDBC/JPQL needed for
 * something this simple, which is exactly the case where that generation
 * pays for itself instead of becoming "magic no one can debug."
 *
 * The "top 100" cap here is a *view* limit, not a retention/deletion
 * policy — old rows are never automatically purged by these queries.
 * Actual retention (e.g. a scheduled job deleting rows past a TTL) is a
 * deliberately separate, later concern.
 */
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

    List<LogEntry> findTop100ByOrderByTimestampDesc();

    List<LogEntry> findTop100ByServiceOrderByTimestampDesc(String service);
}
