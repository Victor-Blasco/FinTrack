# 📄 Product Specification: auth-service

## 1. Description
The **auth-service** handles user registration, identity management, and credential verification. It acts as the Identity Provider (IdP) for the ecosystem, issuing signed JSON Web Tokens (JWT) for downstream authorization.

## 2. Actors
*   **User:** Registers accounts, signs in, and obtains session tokens.
*   **API Gateway / BFF:** Validates issued JWT tokens before routing traffic to core microservices.

## 3. Functional Requirements (EARS Notation)

### 3.1 Account Creation & Login
*   **REQ-AUT-01 (Ubiquitous):** The service shall expose a POST endpoint `/api/v1/auth/register` to handle new user registrations.
*   **REQ-AUT-02 (Unwanted Behavior):** IF an email address is already registered in the system, THEN the service shall reject the registration request with HTTP `409 Conflict`.
*   **REQ-AUT-03 (Ubiquitous):** The service shall expose a POST endpoint `/api/v1/auth/login` to authenticate users using email and password credentials.
*   **REQ-AUT-04 (Unwanted Behavior):** IF login credentials do not match any active user records, THEN the service shall reject the authentication request with HTTP `401 Unauthorized`.

### 3.2 Token Issuance
*   **REQ-AUT-05 (Event-Driven):** WHEN a user logs in successfully, the service shall issue a signed JSON Web Token (JWT) containing the `userId`, roles, and an expiration timestamp (claims).
*   **REQ-AUT-06 (Ubiquitous):** The service shall expose a GET endpoint `/api/v1/auth/validate` for internal Gateway validation checks.
