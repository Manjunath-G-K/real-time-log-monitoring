# Architecture Overview

## Components
1. Log producers (applications)
2. Backend log ingestion service
3. Web dashboard

## Data Flow
1. Applications send logs to backend
2. Backend processes logs in memory
3. Logs are streamed to dashboard in real time
