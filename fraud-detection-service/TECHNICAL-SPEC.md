# 🛠️ Technical Specification: fraud-detection-service

## 1. Stream Consumer: `raw-transactions`
*   **Topic:** `raw-transactions`
*   **Target Payload:**
    ```json
    {
      "transactionId": "uuid-string",
      "userId": "uuid-string",
      "accountNumber": "string",
      "amount": 120.00,
      "currency": "EUR",
      "merchant": "MERCADONA DE VIGO",
      "timestamp": "2026-07-07T20:18:00Z"
    }
    ```

---

## 2. Fraud Evaluation Engine

The engine runs purely in memory to maintain the <50ms latency SLA (RNF-2).

### 2.1 State Management (Redis or Local Cache)
*   To evaluate the **High Frequency Rule**, the service maintains a sliding window index of timestamps per `userId` in a cache (e.g., Redis or an in-memory thread-safe cache like Caffeine).
*   **Key:** `fraud:frequency:{userId}`
*   **Value:** List of transaction timestamps.
*   **TTL:** 60 seconds.

### 2.2 Rules Evaluation Logic
1.  **High Amount Check:**
    ```java
    boolean isHighAmount = event.getAmount() > 2000.0;
    ```
2.  **Frequency Check:**
    *   Add incoming timestamp to the `userId` list.
    *   Evict any elements in the list older than `incomingTimestamp - 60 seconds`.
    *   Evaluate:
        ```java
        boolean isHighFrequency = timestampList.size() > 3;
        ```

---

## 3. Event Producers (Kafka)

### 3.1 Topic: `fraud-verdicts`
*   **Serializer:** JSON
*   **Payload Output Schema:**
    ```json
    {
      "transactionId": "uuid-string",
      "userId": "uuid-string",
      "verdict": "CLEAN" | "SUSPICIOUS",
      "reasons": ["HIGH_AMOUNT_RULE", "HIGH_FREQUENCY_RULE"],
      "evaluatedAt": "2026-07-07T20:18:01Z"
    }
    ```

### 3.2 Topic: `fraud-alerts`
Only broadcasted when `verdict` is `SUSPICIOUS`.
*   **Serializer:** JSON
*   **Payload Output Schema:**
    ```json
    {
      "transactionId": "uuid-string",
      "userId": "uuid-string",
      "amount": 2500.00,
      "merchant": "MERCADONA DE VIGO",
      "reasons": ["HIGH_AMOUNT_RULE"],
      "triggeredAt": "2026-07-07T20:18:01Z"
    }
    ```
*   **Kafka Configuration:** Configure partition keys as `userId` to ensure sequential delivery to downstream consumers and websockets gateways.
