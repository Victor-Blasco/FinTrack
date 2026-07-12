# 📊 FinTrack & FraudShield Ecosystem

Welcome to the **FinTrack & FraudShield Ecosystem**, an enterprise-grade financial technology platform built using **Java 21/25 (Spring Boot 4.x)** and **Next.js (v15+)**, designed under a **Microservices** and **Event-Driven Architecture (EDA)** pattern.

The platform provides a consolidated dashboard for personal finance management while running real-time, low-latency transaction fraud analysis.

---

## 🚀 Quick Start

### 📋 Prerequisites
*   **Java Development Kit (JDK):** Version 21 or 25 (Eclipse Temurin LTS recommended).
*   **Node.js:** LTS version (v20+ or v22+).
*   **Docker & Docker Compose:** Installed and running.
*   **Maven:** Managed via [mvnw](file:///C:/Users/Victor/IdeaProjects/FinTrack/mvnw) / [mvnw.cmd](file:///C:/Users/Victor/IdeaProjects/FinTrack/mvnw.cmd).

### 1. Configure the Environment
Check the environment configurations in the root [.env](file:///C:/Users/Victor/IdeaProjects/FinTrack/.env) file.

### 2. Launch Infrastructure Services
Spin up the PostgreSQL database and Apache Kafka broker (in KRaft mode) using:
```powershell
docker-compose up -d
```
Access the Kafka visual monitor at [http://localhost:8080](http://localhost:8080).

### 3. Build the Backend Microservices
Run the following Maven command at the root directory to compile and package all modules:
```powershell
./mvnw clean package -DskipTests
```

### 4. Run the Web Client
Navigate to the web client module, install packages, and start the development server:
```powershell
cd fintrack-web-client
npm install
npm run dev
```
Open [http://localhost:3000](http://localhost:3000) to access the dashboard.

---

## 🏛️ Project Directory Structure

The repository is structured as a multi-module Maven project for the backend and a separate Next.js project for the frontend client:

*   [auth-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/auth-service) — Handles user authentication, security, and JWT generation.
*   [banking-ingest-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/banking-ingest-service) — Exposes Webhook endpoints for PSD2/Open Banking data streams and multipart file upload for CSV transaction sheets.
*   [finance-profile-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/finance-profile-service) — Manages user financial accounts, wallets, and budget thresholds. Owners the PostgreSQL database.
*   [fraud-detection-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/fraud-detection-service) — Evaluation engine checking transaction velocity and amounts.
*   [categorization-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/categorization-service) — Evaluates incoming merchants and maps them to expense categories.
*   [fintrack-web-client](file:///C:/Users/Victor/IdeaProjects/FinTrack/fintrack-web-client) — React/Next.js dashboard using server-rendered charts and real-time alerts.

---

## 📚 Documentation & Guidelines

*   **[ARCHITECTURE.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/ARCHITECTURE.md)** — Architectural design, system flows, Kafka schemas, database mapping, and observability.
*   **[CONTRIBUTING.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/CONTRIBUTING.md)** — Standards on how to write code, branching models, and testing strategies.
*   **[.agents/rules/](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/)** — AI agent configuration rules, coding guidelines, and technical definitions:
    *   [tech_stack.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/tech_stack.md) — Exact versions, ports, and parameters.
    *   [coding_guidelines.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/coding_guidelines.md) — Style guide for Java and Next.js.
    *   [summary_sdd_project.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/summary_sdd_project.md) — Resumen del proyecto y metodología SDD.
    *   [agent_skills.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/agent_skills.md) — Standard execution commands.
