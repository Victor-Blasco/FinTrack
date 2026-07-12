# 📄 Product Specification: banking-ingest-service

## 1. Description
The **banking-ingest-service** is responsible for consuming financial transaction data from external Open Banking APIs (PSD2) or via manual CSV imports. It acts as the gateway to the streaming pipeline, validating all inputs and broadcasting raw transaction events to the event broker.

## 2. Actors
*   **Sandbox PSD2 / Open Banking Aggregator:** Emits real-time transactional webhooks to the ingest service.
*   **User:** Uploads bank statement CSV files to the web dashboard.

## 3. Functional Requirements (EARS Notation)

### 3.1 Real-Time API Ingestion (Webhooks)
*   **REQ-ING-01 (Ubiquitous):** The service shall expose a secure POST endpoint `/api/v1/ingest/webhook` to receive transaction webhooks.
*   **REQ-ING-02 (Event-Driven):** WHEN a webhook payload is received, the service shall validate the digital signature and standard format JSON (Berlin Group NextGenPSD2).
*   **REQ-ING-03 (Unwanted Behavior):** IF the webhook signature or JSON format is invalid, THEN the service shall reject the request with HTTP `400 Bad Request` and log the validation error.
*   **REQ-ING-04 (Event-Driven):** WHEN a valid transaction payload is received, the service shall generate a context-propagated `traceId` and publish the transaction event to the `raw-transactions` Kafka topic immediately.

### 3.2 CSV Mass Import
*   **REQ-ING-05 (Ubiquitous):** The service shall expose a POST endpoint `/api/v1/ingest/csv` accepting `multipart/form-data` uploads.
*   **REQ-ING-06 (Event-Driven):** WHEN a CSV file is uploaded, the service shall generate a unique `batchId` and compute the SHA-256 hash of the file.
*   **REQ-ING-07 (Unwanted Behavior):** IF the computed SHA-256 hash matches an already processed file in the database, THEN the service shall reject the upload with HTTP `409 Conflict`.
*   **REQ-ING-08 (Event-Driven):** WHEN a valid and unique CSV is uploaded, the service shall respond immediately with HTTP `202 Accepted` containing the `batchId` and dispatch the processing task asynchronously.
*   **REQ-ING-09 (Unwanted Behavior):** IF a CSV row is corrupted or malformed, THEN the service shall log the parsing exception, write the record details to the `csv_batches_audit` table, and continue processing the remaining rows without aborting.
