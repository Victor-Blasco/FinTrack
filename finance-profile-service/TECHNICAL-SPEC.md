# 🛠️ Technical Specification: finance-profile-service

## 1. Database Model (PostgreSQL)

### 1.1 Table: `accounts`
*   `id` (UUID, Primary Key)
*   `user_id` (UUID, UNIQUE index)
*   `account_number` (VARCHAR(30))
*   `balance` (NUMERIC(15, 2)) — Balance total.

### 1.2 Table: `transactions`
*   `id` (UUID, Primary Key) — Matches the original `transactionId`.
*   `account_id` (UUID, Foreign Key referencing `accounts.id`)
*   `amount` (NUMERIC(15, 2))
*   `currency` (VARCHAR(3))
*   `merchant` (VARCHAR(255))
*   `status` (VARCHAR(20)) — ENUM: `PENDING`, `PROCESSED`, `QUARANTINED`.
*   `category` (VARCHAR(20)) — ENUM: `UNASSIGNED`, `ALIMENTACION`, `OCIO`, `SALUD_DEPORTE`, `TRANSPORTE`, `VIVIENDA`, `OTROS`.
*   `timestamp` (TIMESTAMP)

### 1.3 Table: `budgets`
*   `id` (UUID, Primary Key)
*   `user_id` (UUID, Foreign Key)
*   `category` (VARCHAR(20))
*   `monthly_limit` (NUMERIC(15, 2))
*   `accumulated_spend` (NUMERIC(15, 2))

---

## 2. API Endpoints

### 2.1 Get Account Summary
*   **Method / Path:** `GET /api/v1/accounts/summary`
*   **Response Payload:**
    ```json
    {
      "accountId": "uuid-string",
      "balance": 1520.40,
      "currency": "EUR"
    }
    ```

### 2.2 Create Budget
*   **Method / Path:** `POST /api/v1/budgets`
*   **Request Payload:**
    ```json
    {
      "category": "ALIMENTACION",
      "monthlyLimit": 500.00
    }
    ```

---

## 3. Asynchronous Handlers (Kafka Consumers & Producers)

### 3.1 Consumer: `raw-transactions`
Persists the transaction immediately as `PENDING`/`UNASSIGNED` to avoid loss of data.

### 3.2 Consumer: `categorized-events`
Updates the `category` column of the matching transaction row.

### 3.3 Consumer: `fraud-verdicts`
*   IF `verdict` is `CLEAN`:
    1.  Update status to `PROCESSED`.
    2.  Execute atomic balance update:
        ```sql
        UPDATE accounts SET balance = balance + :amount WHERE id = :accountId
        ```
    3.  Increment monthly budget accumulated spend:
        ```sql
        UPDATE budgets SET accumulated_spend = accumulated_spend + :amount 
        WHERE user_id = :userId AND category = :category
        ```
*   IF `verdict` is `SUSPICIOUS`:
    1.  Update status to `QUARANTINED`.
    2.  (Optional) If balance or budget were prematurely updated, revert the values.

### 3.4 Producer: `budget-alerts`
*   **Topic:** `budget-alerts`
*   **Payload Output Schema:**
    ```json
    {
      "userId": "uuid-string",
      "category": "ALIMENTACION",
      "alertLevel": "WARNING_80_PERCENT" | "EXCEEDED_100_PERCENT",
      "limit": 500.00,
      "accumulated": 402.50,
      "timestamp": "2026-07-07T20:18:30Z"
    }
    ```
