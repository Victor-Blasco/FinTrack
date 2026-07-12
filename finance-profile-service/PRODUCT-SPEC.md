# 📄 Product Specification: finance-profile-service

## 1. Description
The **finance-profile-service** acts as the core transactional ledger of the FinTrack system. It manages user accounts, tracks transaction history, monitors budget thresholds, and performs balance calculations.

## 2. Actors
*   **User:** Defines and reviews budget limits, checks account balances, and views expense lists.
*   **System (Kafka Consumer):** Processes raw transaction events, fraud verdicts, and category mappings.

## 3. Functional Requirements (EARS Notation)

### 3.1 Initial Ledger Audit
*   **REQ-PRF-01 (Ubiquitous):** The service shall consume incoming transaction messages from the `raw-transactions` Kafka topic.
*   **REQ-PRF-02 (Event-Driven):** WHEN a raw transaction is consumed, the service shall immediately save it to the database with a state of `PENDING` and category set to `UNASSIGNED`, ensuring all records are persistent before processing.

### 3.2 Category Application
*   **REQ-PRF-03 (Ubiquitous):** The service shall consume categorization updates from the `categorized-events` Kafka topic.
*   **REQ-PRF-04 (Event-Driven):** WHEN a categorized event is received, the service shall update the transaction's category field in the database.

### 3.3 Fraud Verdict Application
*   **REQ-PRF-05 (Ubiquitous):** The service shall consume fraud evaluation results from the `fraud-verdicts` Kafka topic.
*   **REQ-PRF-06 (Event-Driven):** WHEN a fraud verdict with status `CLEAN` is consumed, the service shall update the transaction's state in PostgreSQL to `PROCESSED`, update the user's account balance, and add the transaction's value to the monthly budget calculations.
*   **REQ-PRF-07 (Event-Driven):** WHEN a fraud verdict with status `SUSPICIOUS` is consumed, the service shall update the transaction's state in PostgreSQL to `QUARANTINED` and omit or revert any impact on account balances or budget limits.

### 3.4 Budget Monitoring
*   **REQ-PRF-08 (State-Driven):** WHILE updating a user's monthly budget total, IF the accumulated expense in a category reaches or exceeds 80% (but is less than 100%) of the budget limit, THEN the service shall emit an alert event to the `budget-alerts` Kafka topic.
*   **REQ-PRF-09 (State-Driven):** WHILE updating a user's monthly budget total, IF the accumulated expense in a category reaches or exceeds 100% of the budget limit, THEN the service shall emit a critical alert event to the `budget-alerts` Kafka topic.
