package com.ingestion_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * The persisted record of one ingested log line.
 *
 * Responsibility: hold exactly what a stored log needs to be found again
 * later — who sent it, how severe it is, when it arrived, and its
 * encrypted content — nothing about *how* it got encrypted or *how* it's
 * queried. Replaces the old design where the store held a bare
 * ciphertext String with no service/level/time attached, which made
 * filtering or reasoning about "whose logs are these" impossible.
 *
 * {@code ciphertext} stores the full self-describing envelope produced by
 * CryptoService ("keyVersion:iv:ciphertext"), not just the raw bytes —
 * this entity doesn't need to know that format, it's just an opaque
 * string as far as persistence is concerned.
 */
@Entity
@Table(name = "log_entries")
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String service;

    @Column(nullable = false)
    private String level;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, length = 4000)
    private String ciphertext;

    /** Required by JPA — it builds entities via reflection, not this constructor. */
    protected LogEntry() {
    }

    public LogEntry(String service, String level, Instant timestamp, String ciphertext) {
        this.service = service;
        this.level = level;
        this.timestamp = timestamp;
        this.ciphertext = ciphertext;
    }

    public Long getId() {
        return id;
    }

    public String getService() {
        return service;
    }

    public String getLevel() {
        return level;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getCiphertext() {
        return ciphertext;
    }
}
