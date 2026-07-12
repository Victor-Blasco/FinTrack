# 🛠️ Technical Specification: categorization-service

## 1. Kafka Consumer: `fraud-verdicts`
*   **Topic:** `fraud-verdicts`
*   **Deserializer:** `org.springframework.kafka.support.serializer.JsonDeserializer`
*   **Target Payload:**
    ```json
    {
      "transactionId": "uuid-string",
      "userId": "uuid-string",
      "verdict": "CLEAN",
      "reasons": [],
      "evaluatedAt": "2026-07-07T20:18:00Z"
    }
    ```
*   **Behavior:** Ignore messages where `verdict` is `SUSPICIOUS`. Proceed with categorization when `verdict` is `CLEAN`.

---

## 2. Categorization Rules & Patterns

The service evaluates the `merchant` string case-insensitively using regex or pattern matching.

### 2.1 Mappings table
| Pattern Rule | Categorization Result |
| :--- | :--- |
| `(?i).*(mercadona|carrefour|dia|lidl|supermercado).*` | `ALIMENTACION` |
| `(?i).*(netflix|spotify|cine|bar|restaurante|pub|club).*` | `OCIO` |
| `(?i).*(farmacia|gimnasio|dentista|decathlon|doctor).*` | `SALUD_DEPORTE` |
| `(?i).*(uber|cabify|renfe|gasolinera|repsol|metro|bus).*` | `TRANSPORTE` |
| `(?i).*(iberdrola|naturgy|alquiler|comunidad|endesa|agua).*` | `VIVIENDA` |
| *No Match* | `OTROS` |

---

## 3. Kafka Producer: `categorized-events`
*   **Topic:** `categorized-events`
*   **Payload Output Schema:**
    ```json
    {
      "transactionId": "uuid-string",
      "category": "ALIMENTACION",
      "categorizedAt": "2026-07-07T20:18:05Z"
    }
    ```

---

## 4. Technical Constraints
*   **Stateless Processing:** This service is entirely stateless. It does not connect to a relational database.
*   **Kafka Context Propagation:** Ensure that OpenTelemetry trace contexts received from the consumer metadata are copied to the outgoing Kafka producer record headers.
