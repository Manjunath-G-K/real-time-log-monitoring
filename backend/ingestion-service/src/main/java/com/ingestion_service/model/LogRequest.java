package com.ingestion_service.model;

public class LogRequest {

    private String service;
    private String message;

    public String getService ( ) {
        return service;
    }

    public void setService ( String service ) {
        this.service = service;
    }

    public String getMessage ( ) {
        return message;
    }

    public void setMessage ( String message ) {
        this.message = message;
    }
}
