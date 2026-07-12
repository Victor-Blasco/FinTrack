# 🛠️ Technical Specification: auth-service

## 1. Database Model (PostgreSQL)

### 1.1 Table: `users`
*   `id` (UUID, Primary Key)
*   `email` (VARCHAR(150), UNIQUE Index)
*   `password_hash` (VARCHAR(255))
*   `role` (VARCHAR(20)) — default `ROLE_USER`.
*   `created_at` (TIMESTAMP)

---

## 2. API Endpoints

### 2.1 Register User
*   **Method / Path:** `POST /api/v1/auth/register`
*   **Request Payload:**
    ```json
    {
      "email": "user@fintrack.com",
      "password": "SecurePassword123"
    }
    ```
*   **Response (`201 Created`):**
    ```json
    {
      "userId": "uuid-string",
      "email": "user@fintrack.com",
      "createdAt": "2026-07-07T20:18:00Z"
    }
    ```

### 2.2 Login User
*   **Method / Path:** `POST /api/v1/auth/login`
*   **Request Payload:**
    ```json
    {
      "email": "user@fintrack.com",
      "password": "SecurePassword123"
    }
    ```
*   **Response (`200 OK`):**
    ```json
    {
      "accessToken": "eyJhbGciOi...",
      "tokenType": "Bearer",
      "expiresIn": 3600
    }
    ```

### 2.3 Validate Token
*   **Method / Path:** `GET /api/v1/auth/validate`
*   **Request Headers:**
    *   `Authorization`: `Bearer <token>`
*   **Response (`200 OK`):**
    ```json
    {
      "userId": "uuid-string",
      "role": "ROLE_USER"
    }
    ```

---

## 3. Cryptography & Token Configuration
*   **Password Encoder:** BCrypt with a strength factor of 12.
*   **Signing Key:** RS256 (Private/Public Key Pair) or HMAC-SHA256 (using the secret defined in [.env](file:///C:/Users/Victor/IdeaProjects/FinTrack/.env)).
*   **Token Expiration:** 1 Hour (3600 seconds).
*   **Required JWT Claims:**
    *   `sub`: Subject (`userId`).
    *   `email`: User's email.
    *   `role`: User permission role.
    *   `iat`: Issued-at timestamp.
    *   `exp`: Expiration timestamp.
