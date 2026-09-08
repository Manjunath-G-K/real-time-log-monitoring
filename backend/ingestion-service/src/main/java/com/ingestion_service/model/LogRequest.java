package com.ingestion_service.model;

/**
 * What a producer POSTs to /logs.
 *
 * `level` is new: previously there was no severity field at all, so every
 * log was implicitly the same importance. It's optional here (defaults to
 * "INFO" in LogService) rather than required — adding real validation
 * (@NotBlank, an enum of allowed levels, etc.) is a separate, later step;
 * this class intentionally still has no annotations of its own yet.
 */
public class LogRequest {

    private String service;
    private String level;
    private String message;

    public String getService ( ) {
        return service;
    }

    public void setService ( String service ) {
        this.service = service;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage ( ) {
        return message;
    }

    public void setMessage ( String message ) {
        this.message = message;
    }
}
