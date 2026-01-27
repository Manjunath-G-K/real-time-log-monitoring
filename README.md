# Real-Time Log Monitoring System

> A learning-focused backend project to understand real-time log ingestion, basic security concepts, and system design.

## Problem
Applications generate logs that may contain sensitive information. Storing logs in plain text
can lead to security issues during breaches.

## Solution
This project collects logs from multiple services, processes them securely in memory,
and streams them live to a dashboard without permanent storage.

## Key Goals
- Secure log ingestion with basic encryption
- Clean backend layering (controller-service separation)
- Minimal data retention


## Tech Stack (Planned)
- Backend: Java (Spring Boot)
- Frontend: React
- Communication: REST + WebSockets
- Storage: In-memory

## Project Status
🚧 Day 1: Project setup and planning
🚧  Day 2: Implemented backend log ingestion API using Spring Boot
🚧 Day 3: Added service layer and basic encryption for logs
