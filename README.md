# 📊 FinTrack & FraudShield Ecosystem

Welcome to the **FinTrack & FraudShield Ecosystem**, an enterprise-grade financial technology platform built using **Java 21/25 (Spring Boot 4.x)** and **Next.js (v16+)**, designed under a **Microservices** and **Event-Driven Architecture (EDA)** pattern.

The platform provides a consolidated dashboard for personal finance management while running real-time, low-latency transaction fraud analysis.

---

## 🚀 Quick Start Guide

### ⚡ Opción Rápida (Un solo comando)
Para levantar **toda la aplicación** (PostgreSQL + Kafka + los 5 Microservicios + Cliente Web Next.js) automáticamente, ejecuta en PowerShell:

```powershell
.\start-all.ps1
```

*Para detener todo el ecosistema:*
```powershell
.\stop-all.ps1
```

---

### 📋 Prerequisites
* **Java Development Kit (JDK):** Version 21 or 25 (Eclipse Temurin LTS recommended).
* **Node.js:** LTS version (v20+ or v22+).
* **Docker & Docker Compose:** Installed and running.
* **Maven:** Managed via [mvnw](file:///C:/Users/Victor/IdeaProjects/FinTrack/mvnw) / [mvnw.cmd](file:///C:/Users/Victor/IdeaProjects/FinTrack/mvnw.cmd).

---

### 1. Configure the Environment
Verify the environment configurations in the root [.env](file:///C:/Users/Victor/IdeaProjects/FinTrack/.env) file.

### 2. Launch Infrastructure Services (PostgreSQL & Apache Kafka)
Spin up the PostgreSQL database and Apache Kafka broker (in KRaft mode) using:
```powershell
docker-compose up -d
```
* **PostgreSQL:** Running at `localhost:5432` (`fintrack_db`).
* **Kafka UI:** Access the visual Kafka monitor at [http://localhost:8080](http://localhost:8080).

### 3. Build Backend Microservices
Run the following Maven command at the root directory to compile and package all modules:
```powershell
./mvnw clean package -DskipTests
```

### 4. Run the Backend Microservices
Each Spring Boot microservice operates independently on its allocated port:

| Microservice | Port | Description | Startup Command |
| :--- | :--- | :--- | :--- |
| **`auth-service`** | `8081` | Authentication & JWT Tokens | `java -jar auth-service/target/auth-service-0.0.1-SNAPSHOT.jar` |
| **`banking-ingest-service`** | `8082` | Webhooks PSD2 & CSV Ingestion | `java -jar banking-ingest-service/target/banking-ingest-service-0.0.1-SNAPSHOT.jar` |
| **`finance-profile-service`** | `8083` | Ledger, Budgets & Accounts | `java -jar finance-profile-service/target/finance-profile-service-0.0.1-SNAPSHOT.jar` |
| **`fraud-detection-service`** | `8084` | Real-time Fraud Analysis | `java -jar fraud-detection-service/target/fraud-detection-service-0.0.1-SNAPSHOT.jar` |
| **`categorization-service`** | `8085` | Expense Categorization Engine | `java -jar categorization-service/target/categorization-service-0.0.1-SNAPSHOT.jar` |

*To run a single service via Maven during development:*
```powershell
./mvnw spring-boot:run -pl auth-service
```

### 5. Run the Web Client (Next.js Dashboard)
Navigate to the web client module, install packages if needed, and start the development server:
```powershell
cd fintrack-web-client
npm install
npm run dev
```
Open [http://localhost:3000](http://localhost:3000) to access the dashboard.

---

## 🏛️ Project Directory Structure

The repository is structured as a multi-module Maven project for the backend and a separate Next.js project for the frontend client:

* [auth-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/auth-service) — Handles user authentication, security, and JWT generation (Port `8081`).
* [banking-ingest-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/banking-ingest-service) — Exposes Webhook endpoints for PSD2/Open Banking data streams and CSV transaction sheets (Port `8082`).
* [finance-profile-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/finance-profile-service) — Manages user financial accounts, wallets, and budget thresholds (Port `8083`).
* [fraud-detection-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/fraud-detection-service) — Evaluation engine checking transaction velocity and amounts (Port `8084`).
* [categorization-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/categorization-service) — Evaluates incoming merchants and maps them to expense categories (Port `8085`).
* [fintrack-web-client](file:///C:/Users/Victor/IdeaProjects/FinTrack/fintrack-web-client) — Next.js 16+ React dashboard with real-time alerts and clean UI design system (Port `3000`).

---

## 📚 Documentation & Guidelines

* **[ARCHITECTURE.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/ARCHITECTURE.md)** — Architectural design, system flows, Kafka schemas, database mapping, and observability.
* **[CONTRIBUTING.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/CONTRIBUTING.md)** — Standards on how to write code, branching models, and testing strategies.
* **[.agents/rules/](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/)** — AI agent configuration rules and governance:
    * [ui_design_guide.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/ui_design_guide.md) — Mandatory UI Design System (colors, fonts, layout rules).
    * [tech_stack.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/tech_stack.md) — Exact versions, ports, and parameters.
    * [coding_guidelines.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/coding_guidelines.md) — Style guide for Java and Next.js.
    * [summary_sdd_project.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/summary_sdd_project.md) — Resumen del proyecto y metodología SDD.