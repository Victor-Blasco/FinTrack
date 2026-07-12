# 📄 Product Specification: fraud-detection-service

## 1. Description
The **fraud-detection-service** (FraudShield) is a real-time, low-latency stream evaluator. It consumes raw transactions as they are ingested, applies deterministic fraud check rules in parallel, and broadcasts security verdicts and user alerts.

## 2. Actors
*   **System (Kafka Consumer):** Processes raw transaction streams in real-time.

## 3. Functional Requirements (EARS Notation)

### 3.1 Stream Processing & Evaluation
*   **REQ-FRD-01 (Ubiquitous):** The service shall consume transaction events from the `raw-transactions` Kafka topic in parallel.
*   **REQ-FRD-02 (Ubiquitous):** The service shall process each transaction evaluation within a maximum latency window of 50 milliseconds from the message read time.

### 3.2 Deterministic Fraud Rules
*   **REQ-FRD-03 (State-Driven):** WHILE evaluating a transaction, IF the single transaction `amount` exceeds 2,000.00 EUR, THEN the service shall flag the transaction with a `SUSPICIOUS` verdict and note the reason as `HIGH_AMOUNT_RULE`.
*   **REQ-FRD-04 (State-Driven):** WHILE evaluating a transaction, IF the `userId` records more than 3 transactions within an interval window of less than 60 seconds, THEN the service shall flag the transaction with a `SUSPICIOUS` verdict and note the reason as `HIGH_FREQUENCY_RULE`.
*   **REQ-FRD-05 (Ubiquitous):** The service shall issue a `CLEAN` verdict if none of the active fraud check rules are triggered.

### 3.3 Alert Dispatch
*   **REQ-FRD-06 (Ubiquitous):** The service shall publish the final evaluation to the `fraud-verdicts` Kafka topic.
*   **REQ-FRD-07 (Event-Driven):** WHEN a transaction receives a `SUSPICIOUS` verdict, the service shall publish a high-priority alarm notification to the `fraud-alerts` Kafka topic to trigger UI alert floating toasts.
