# 📄 Product Specification: fintrack-web-client

## 1. Description
The **fintrack-web-client** is the user-facing web interface. Built with Next.js, it aggregates views for financial account details, budgets, transactions, and handles real-time alerts. It also contains the Backend-for-Frontend (BFF) Gateway layer which routes requests, validates auth tokens, and feeds microservices.

## 2. Actors
*   **User (Actor Principal):** Manages budgets, uploads CSV file sheets, and reviews expense trends.
*   **System (Notification Client):** Listens for WebSockets/SSE channels to display immediate toasts.

## 3. Functional Requirements (EARS Notation)

### 3.1 Financial Dashboard
*   **REQ-WEB-01 (Ubiquitous):** The dashboard shall display the user's total balance, monthly spending sums, and expense categories.
*   **REQ-WEB-02 (Ubiquitous):** The client shall display spending distribution charts using React chart libraries.

### 3.2 CSV Uploader File Form
*   **REQ-WEB-03 (Ubiquitous):** The client shall provide a Drag & Drop file input zone supporting `.csv` extensions.
*   **REQ-WEB-04 (Event-Driven):** WHEN the CSV file is uploaded, the web client shall submit it to the BFF, and display a "Processing" status bar upon receiving HTTP `202 Accepted`.
*   **REQ-WEB-05 (Event-Driven):** WHEN processing finishes, the web client shall display an audit log overview (e.g., "100 succeeded, 5 failed").

### 3.3 Real-Time Floating Toasts
*   **REQ-WEB-06 (Event-Driven):** WHEN a critical `fraud-alerts` or `budget-alerts` event is received via WebSockets/SSE, the web client shall render a floating toast alert (Green/Yellow/Red indicator).

### 3.4 API Gateway BFF (Security)
*   **REQ-WEB-07 (Ubiquitous):** The Gateway shall intercept all requests and validate the Authorization token.
*   **REQ-WEB-08 (Event-Driven):** WHEN a request has a valid token, the Gateway shall inject the `X-User-Id` header into downstream microservice requests.
*   **REQ-WEB-09 (Unwanted Behavior):** IF a request does not contain a valid bearer token, THEN the Gateway shall block the call, returning HTTP `401 Unauthorized`.
