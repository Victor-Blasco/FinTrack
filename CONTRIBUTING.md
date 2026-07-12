# 🤝 Contributing to FinTrack

Thank you for contributing to the FinTrack & FraudShield Ecosystem. To maintain engineering excellence, please read and follow these standards.

---

## 🛠️ Development Guidelines

### Java & Spring Boot (Backend Services)
*   **Java version:** `25` (Eclipse Temurin LTS recommended, compiled with Lombok support).
*   **Layering:** Maintain clear boundary separations. Controllers handle validation and mapping; Services contain business logic; Repositories deal with database records.
*   **Security:** Every REST Controller route must enforce role validations via OAuth2 / Spring Security JWT controls.
*   **Exceptions:** Throw domain-specific runtime exceptions. All APIs must use a `@RestControllerAdvice` to serialize errors safely.
*   **Constructors:** Prefer constructor injection. Use Lombok's `@RequiredArgsConstructor` on final fields.

### Next.js & TypeScript (Frontend Client)
*   **Conventions:** Next.js App Router rules apply. Place pages under [fintrack-web-client/app](file:///C:/Users/Victor/IdeaProjects/FinTrack/fintrack-web-client/app).
*   **Types:** Use strong typings for all React components and REST API contracts. Avoid `any` declarations.
*   **Rendering:** Use React Server Components (RSC) where possible. Mark interactivity boundaries using `'use client'`.

---

## 🧪 Testing Requirements

### 1. Contract Testing (Spring Cloud Contract / Pact)
*   Any modification to Kafka JSON models or REST request/response bodies MUST pass contract verification tests.
*   Changes that break downstream consumers will be automatically rejected by the CI/CD pipeline.

### 2. Unit and Integration Tests
*   **Backend:** Write JUnit 5 tests for your business service implementations. Use `@SpringBootTest` and `MockMvc` for controller testing.
*   **Frontend:** Run ESLint and TypeScript compilation checks before committing.
*   Run tests across all backend microservices from the project root using:
    ```powershell
    ./mvnw test
    ```

---

## 🚀 Branching & Commits

*   **Branch Naming:** Follow `feature/` or `bugfix/` prefix naming conventions:
    *   `feature/auth-service-login`
    *   `bugfix/fraud-detection-latency`
*   **Commit Messages:** Commit messages should be clear, declarative, and present-tense (e.g. `feat: add quarantine status to transaction record`).
*   **Pull Requests:** All PRs must pass compile and test suites. Code reviews require approval from at least one core maintainer.
