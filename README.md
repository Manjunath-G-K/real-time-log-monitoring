# Real-Time Log Monitoring System

> A learning-focused backend project to understand real-time log ingestion, basic security concepts, and system design.

## Problem
Applications generate logs that may contain sensitive information such as emails and phone numbers.
Storing logs in plain text can lead to security issues during breaches.

## Solution
This project ingests logs from multiple services, masks sensitive data, encrypts log messages,
and processes them securely in memory. Real-time streaming to a dashboard is planned.

## Key Goals
- Secure log ingestion with encryption and sensitive data masking
- Clean backend layering (controller-service separation)
- Minimal data retention

## Tech Stack
- Backend: Java (Spring Boot)
- Frontend: React (planned)
- Communication: REST (WebSockets planned)
- Storage: In-memory

## Project Status
✅ Day 1: Project setup and planning  
✅ Day 2: Implemented backend log ingestion API using Spring Boot  
✅ Day 3: Added service layer and basic encryption for logs  
✅ Day 4: Implemented email and phone masking for sensitive data
