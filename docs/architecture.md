# Architecture Overview

## Components
1. Log producers (applications)
2. Backend log ingestion service (Spring Boot)
3. Web dashboard (planned)

## Data Flow
1. Applications send logs to backend via REST API
2. Backend processes logs using a service layer
3. Log messages are encrypted and handled in memory

## Current Implementation (Day 5)
- REST-based log ingestion endpoint
- Service-layer processing for logs
- Basic encryption applied before handling logs
- Logs are stored temporarily in an in-memory sliding window (last 100 logs)


## Planned Enhancements
- Real-time log streaming using WebSockets
- Sensitive data masking (email, phone)
- Frontend dashboard for live logs
