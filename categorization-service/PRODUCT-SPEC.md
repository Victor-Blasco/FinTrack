# 📄 Product Specification: categorization-service

## 1. Description
The **categorization-service** is an asynchronous analytical microservice that processes transaction merchant details to assign appropriate financial expense categories (such as Alimentación, Ocio, Vivienda). It acts as an event enricher in the transactional pipeline.

## 2. Actors
*   **System (Kafka Consumer):** Listens to transactional veredicto updates from the fraud detection service.

## 3. Functional Requirements (EARS Notation)

### 3.1 Event Ingestion
*   **REQ-CAT-01 (Ubiquitous):** The service shall consume evaluated transactions continuously from the `fraud-verdicts` Kafka topic.
*   **REQ-CAT-02 (Unwanted Behavior):** IF an incoming transaction verdict is marked `SUSPICIOUS`, THEN the service shall discard the message and log the suspension to avoid categorizing fraudulent activities.

### 3.2 Automated Categorization Logic
*   **REQ-CAT-03 (Event-Driven):** WHEN an evaluated transaction marked `CLEAN` is consumed, the service shall analyze the `merchant` string to determine the category mapping.
*   **REQ-CAT-04 (Ubiquitous):** The system shall map merchant text patterns to predefined categories:
    *   **ALIMENTACION:** Merchant matches patterns like `MERCADONA`, `CARREFOUR`, `DIA`, `LIDL`.
    *   **OCIO:** Merchant matches patterns like `NETFLIX`, `SPOTIFY`, `CINE`, `BAR`, `RESTAURANT`.
    *   **SALUD_DEPORTE:** Merchant matches patterns like `FARMACIA`, `GIMNASIO`, `DENTISTA`, `DECATLON`.
    *   **TRANSPORTE:** Merchant matches patterns like `UBER`, `CABIFY`, `RENFE`, `GASOLINERA`, `METRO`.
    *   **VIVIENDA:** Merchant matches patterns like `IBERDROLA`, `NATURGY`, `ALQUILER`, `COMUNIDAD`.
    *   **OTROS:** Executed when no other pattern matches.
*   **REQ-CAT-05 (Event-Driven):** WHEN categorization is complete, the service shall publish a enrichment event consisting of the `transactionId`, `category` value, and timestamp to the `categorized-events` Kafka topic.
