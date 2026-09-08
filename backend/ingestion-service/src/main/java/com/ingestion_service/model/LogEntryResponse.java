package com.ingestion_service.model;

import java.time.Instant;

/**
 * What the API actually hands back for a log entry.
 *
 * Responsibility: decouple the wire format from the JPA entity. Returning
 * {@link LogEntry} straight from a controller is a common trap — it
 * couples your API contract to your table schema, and can trigger lazy-
 * loading serialization surprises once relationships are added. A
 * dedicated response type costs one small class and avoids both.
 *
 * Deliberately still exposes the raw ciphertext, not the decrypted
 * message: there's no auth boundary yet distinguishing who is allowed to
 * read log contents, so nothing decrypts it for an anonymous caller.
 */
public record LogEntryResponse(
        Long id,
        String service,
        String level,
        Instant timestamp,
        String ciphertext) {

    public static LogEntryResponse from(LogEntry entry) {
        return new LogEntryResponse(
                entry.getId(),
                entry.getService(),
                entry.getLevel(),
                entry.getTimestamp(),
                entry.getCiphertext());
    }
}
