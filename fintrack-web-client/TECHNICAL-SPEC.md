# 🛠️ Technical Specification: fintrack-web-client

## 1. UI Routing & Components (Next.js App Router)

### 1.1 Pages Directory Structure
*   `app/page.tsx` — Login / Landpage routing.
*   `app/dashboard/page.tsx` — Main dashboard: displays balances, transaction lists, and charts.
*   `app/dashboard/budgets/page.tsx` — View and configure budgets with color indicator cards.
*   `app/dashboard/upload/page.tsx` — CSV file Drag & Drop uploader with progress tracking.

### 1.2 Layout & Design Theme
*   **Colors (TailwindCSS):** Curated slate/glassmorphism dark palette.
*   **Fonts:** Outfit or Inter fonts from Google Fonts.
*   **Charts:** Recharts package for layout visualizers.

---

## 2. API Gateway BFF & Reverse Proxy
The Next.js Middleware acts as the API Gateway BFF:

### 2.1 Middleware Authorization Rules (`middleware.ts`)
*   Match all core microservice API paths (e.g., `/api/v1/accounts/*`, `/api/v1/budgets/*`, `/api/v1/ingest/*`).
*   Retrieve the `Authorization` bearer token from the incoming client request.
*   Validate the token.
*   If valid:
    *   Extract the `userId` claim.
    *   Forward the request to the target microservice routing destination, injecting the header:
        ```http
        X-User-Id: <userId>
        ```
*   If invalid or missing:
    *   Respond with HTTP `401 Unauthorized` directly at the edge.

---

## 3. Real-Time Push Gateway (Server-Sent Events / WebSockets)
To handle real-time warnings (Toasts) in the browser:

*   The client establishes a single SSE or WebSocket link to the Gateway.
*   The Gateway subscribes to the Kafka topics `fraud-alerts` and `budget-alerts`.
*   Incoming alerts matching the client's `userId` are serialized and pushed to the browser.
*   The client uses a global state (e.g., Zustand) or context listener to trigger notification components.
