# 🏗️ Architecture & Flow – Real-Time Log Monitoring System

## 🌐 What this project does (simple version)

This project collects logs from applications, **masks sensitive data**, **encrypts logs in memory**, and **streams them live** to a browser using WebSockets.
It also has a **panic mode** to instantly clear logs during emergencies.

This is a **learning-focused full-stack project**, not a production system.

---

## 🧩 High-Level Architecture

```text
📦 Application / Client
        |
        |  POST /logs
        v
🧭 LogController (REST API)
        |
        v
⚙️ LogService (Core Logic)
        |
        +── 🔐 Mask + Encrypt
        |
        +── 🧠 Store in Memory
        |
        +── 📊 Update Metrics
        |
        +── 📡 Send to WebSocket
        v
🌍 Browser Dashboard (Live UI)
```

---

## 🔁 Log Flow (Step-by-Step)

1️⃣ Client sends a log using REST API  
2️⃣ Controller receives the request  
3️⃣ Service masks email & phone numbers  
4️⃣ Log is encrypted and stored in memory  
5️⃣ Metrics are updated  
6️⃣ Log is pushed live to UI via WebSocket

✅ Result: Log appears instantly in browser

---

## 📡 Real-Time Streaming Flow (WebSocket)

```text
⚙️ LogService
      |
      | broadcast(log)
      v
📡 WebSocket Server
      |
      v
🌍 Browser (Live Updates)
```

✅ No polling  
✅ Instant updates  
✅ Multiple clients supported

---

## 🚨 Panic Mode Flow

```text
👨‍💻 User / Admin
      |
      | POST /logs/panic
      v
⚙️ LogService.panic()
      |
      +── 🔄 Rotate Encryption Key
      +── 🧹 Clear In-Memory Logs
      +── ♻️ Reset Metrics
      +── 📢 Notify UI
```

✅ Old logs become invalid  
✅ Dashboard is alerted instantly  
✅ System moves to safe state

---

## 🔐 Security (What is actually implemented)

* ✂️ Email & phone masking (regex-based)  
* 🔐 Logs encrypted **before storage**  
* 🧠 No disk storage (memory only)  
* 🚨 Panic mode clears all data

> This simulates **basic incident response**, not enterprise security.

---

## 📊 Metrics Available

* 📈 Total logs received  
* ⏱️ Time of last log  
* ♻️ Metrics reset during panic mode

Accessible via:

```
GET /logs/metrics
```

---

## ⚠️ Limitations (Be honest in interview)

* Single backend instance  
* In-memory storage only  
* No authentication  
* Not horizontally scalable

👉 These are **intentional** for learning clarity.

---

## 🎯 Why this project is good for interviews

* Shows **backend fundamentals**  
* Demonstrates **real-time systems**  
* Uses **WebSockets + REST**  
* Clean separation of layers  
* Easy to explain step-by-step

---
