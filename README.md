# 📊 Ecosistema FinTrack & FraudShield

Bienvenido al **Ecosistema FinTrack & FraudShield**, una plataforma de tecnología financiera de nivel empresarial desarrollada en **Java 21/25 (Spring Boot 4.x)** y **Next.js (v16+)**, diseñada bajo una arquitectura de **Microservicios** y el patrón **Event-Driven Architecture (EDA)**.

La plataforma proporciona un panel unificado para la gestión de finanzas personales a la vez que realiza análisis de fraude de transacciones en tiempo real con baja latencia.

---

## 🚀 Guía de Inicio Rápido

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

### 📋 Requisitos Previos
* **Java Development Kit (JDK):** Versión 21 o 25 (se recomienda Eclipse Temurin LTS).
* **Node.js:** Versión LTS (v20+ o v22+).
* **Docker & Docker Compose:** Instalado y en ejecución.
* **Maven:** Gestionado mediante [mvnw](file:///C:/Users/Victor/IdeaProjects/FinTrack/mvnw) / [mvnw.cmd](file:///C:/Users/Victor/IdeaProjects/FinTrack/mvnw.cmd).

---

### 1. Configurar el Entorno
Verifica las configuraciones de entorno en el archivo [.env](file:///C:/Users/Victor/IdeaProjects/FinTrack/.env) en el directorio raíz.

### 2. Iniciar Servicios de Infraestructura (PostgreSQL & Apache Kafka)
Levanta la base de datos PostgreSQL y el broker Apache Kafka (en modo KRaft) usando:
```powershell
docker-compose up -d
```
* **PostgreSQL:** Ejecutándose en `localhost:5432` (`fintrack_db`).
* **Kafka UI:** Accede al monitor visual de Kafka en [http://localhost:8080](http://localhost:8080).

### 3. Compilar los Microservicios Backend
Ejecuta el siguiente comando Maven en el directorio raíz para compilar y empaquetar todos los módulos:
```powershell
./mvnw clean package -DskipTests
```

### 4. Ejecutar los Microservicios Backend
Cada microservicio Spring Boot funciona de forma independiente en su puerto asignado:

| Microservicio | Puerto | Descripción | Comando de Inicio |
| :--- | :--- | :--- | :--- |
| **`auth-service`** | `8081` | Autenticación y Tokens JWT | `java -jar auth-service/target/auth-service-0.0.1-SNAPSHOT.jar` |
| **`banking-ingest-service`** | `8082` | Webhooks PSD2 e Ingesta CSV | `java -jar banking-ingest-service/target/banking-ingest-service-0.0.1-SNAPSHOT.jar` |
| **`finance-profile-service`** | `8083` | Libro Contable, Presupuestos y Cuentas | `java -jar finance-profile-service/target/finance-profile-service-0.0.1-SNAPSHOT.jar` |
| **`fraud-detection-service`** | `8084` | Real-time Fraud Analysis | `java -jar fraud-detection-service/target/fraud-detection-service-0.0.1-SNAPSHOT.jar` |
| **`categorization-service`** | `8085` | Motor de Categorización de Gastos | `java -jar categorization-service/target/categorization-service-0.0.1-SNAPSHOT.jar` |

*Para ejecutar un solo servicio mediante Maven durante el desarrollo:*
```powershell
./mvnw spring-boot:run -pl auth-service
```

### 5. Ejecutar el Cliente Web (Next.js Dashboard)
Accede al módulo del cliente web, instala los paquetes si es necesario y arranca el servidor de desarrollo:
```powershell
cd fintrack-web-client
npm install
npm run dev
```
Abre [http://localhost:3000](http://localhost:3000) en tu navegador para acceder al panel de control.

---

## 🏛️ Estructura del Directorio del Proyecto

El repositorio está estructurado como un proyecto Maven multiproyecto para el backend y un proyecto Next.js independiente para el cliente del frontend:

* [auth-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/auth-service) — Gestiona la autenticación de usuarios, seguridad y generación de JWT (Puerto `8081`).
* [banking-ingest-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/banking-ingest-service) — Expone endpoints de Webhooks para flujos de datos PSD2/Open Banking e importaciones de transacciones mediante CSV (Puerto `8082`).
* [finance-profile-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/finance-profile-service) — Administra cuentas financieras de usuario, carteras y límites presupuestarios (Puerto `8083`).
* [fraud-detection-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/fraud-detection-service) — Evalúa transacciones de entrada según límites de importe y velocidad del perfil de usuario (Puerto `8084`).
* [categorization-service](file:///C:/Users/Victor/IdeaProjects/FinTrack/categorization-service) — Clasifica los comercios de las transacciones entrantes y los mapea a categorías de gasto (Puerto `8085`).
* [fintrack-web-client](file:///C:/Users/Victor/IdeaProjects/FinTrack/fintrack-web-client) — Panel de control React con Next.js 16+ con alertas en tiempo real y un sistema de diseño de interfaz limpio (Puerto `3000`).

---

## 📚 Documentación y Guías

* **[ARCHITECTURE.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/ARCHITECTURE.md)** — Diseño arquitectónico, flujos del sistema, esquemas de Kafka, mapeo de base de datos y observabilidad.
* **[CONTRIBUTING.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/CONTRIBUTING.md)** — Estándares sobre cómo escribir código, modelos de ramificación de Git y estrategias de prueba.
* **[.agents/rules/](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/)** — Reglas de configuración y gobernanza de agentes de IA:
    * [ui_design_guide.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/ui_design_guide.md) — Sistema de Diseño UI Obligatorio (colores, fuentes y reglas de maquetación).
    * [tech_stack.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/tech_stack.md) — Versiones exactas de dependencias, puertos y parámetros.
    * [coding_guidelines.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/coding_guidelines.md) — Guía de estilo para Java y Next.js.
    * [summary_sdd_project.md](file:///C:/Users/Victor/IdeaProjects/FinTrack/.agents/rules/summary_sdd_project.md) — Resumen de especificaciones del proyecto y metodología SDD.