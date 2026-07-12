# 🛠️ Technical Specification: banking-ingest-service

## 1. API Endpoints

### 1.1 Ingest Webhook
*   **Method / Path:** `POST /api/v1/ingest/webhook`
*   **Request Headers:**
    *   `Signature`: Digital signature header for verification.
    *   `Content-Type`: `application/json`
*   **Request Payload (PSD2 JSON Format):**
    ```json
    {
      "transactionId": "uuid-string",
      "accountNumber": "ES12345678901234567890",
      "amount": 25.50,
      "currency": "EUR",
      "merchant": "MERCADONA DE VIGO",
      "timestamp": "2026-07-07T20:18:00Z"
    }
    ```
*   **Responses:**
    *   `201 Created`: Webhook processed and broadcasted to Kafka.
    *   `400 Bad Request`: Payload validation or signature verification failed.

### 1.2 CSV Upload
*   **Method / Path:** `POST /api/v1/ingest/csv`
*   **Request Headers:**
    *   `Content-Type`: `multipart/form-data`
*   **Request Parameters:**
    *   `file`: The CSV file data stream.
*   **Response Payload (`202 Accepted`):**
    ```json
    {
      "batchId": "uuid-string",
      "status": "PROCESSING",
      "hash": "sha256-hash-value"
    }
    ```
*   **CSV Format Constraints:**
    *   Columns: `transactionId, accountNumber, amount, currency, merchant, timestamp`
    *   Separator: `,` (comma)

---

## 2. Database Model

The service uses a local transactional schema (PostgreSQL) for deduplication and audit logs.

### 2.1 Table: `csv_uploads`
Stores information about processed CSV uploads to prevent duplicates.
*   `id` (UUID, Primary Key)
*   `hash` (VARCHAR(64), UNIQUE Index) — SHA-256 hash of the uploaded CSV file.
*   `filename` (VARCHAR(255))
*   `uploaded_at` (TIMESTAMP)

### 2.2 Table: `csv_batches_audit`
Logs parsing errors for imported CSV lines.
*   `id` (BIGINT, Primary Key, Auto-increment)
*   `batch_id` (UUID, Index)
*   `row_number` (INT)
*   `error_message` (TEXT)
*   `raw_content` (TEXT)
*   `logged_at` (TIMESTAMP)

---

## 3. Event Publisher (Kafka)
*   **Topic Name:** `raw-transactions`
*   **Serializer:** `org.springframework.kafka.support.serializer.JsonSerializer`
*   **Headers:**
    *   `traceparent`: OpenTelemetry trace context header.

---

## 4. Technical Constraints & Design Patterns
*   **Streaming Parser:** Use `OpenCSV` library or `Jackson` to read the multipart file line-by-line using streaming buffers to maintain $O(1)$ memory footprint.
*   **Async Processing:** Decorate the processing method with Spring's `@Async` annotation.
*   **Idempotency Keys:** Ensure the database has a unique constraint on `transactionId` in relevant models.
