/**
 * Módulo cliente HTTP minimalista para la interacción Full-Stack con FinTrack.
 * <p>
 * El navegador adjunta automáticamente las cookies HttpOnly en cada petición.
 * </p>
 *
 * @author Victor Blasco
 */

/**
 * Respuesta DTO de resumen de cuenta bancaria.
 */
export interface ApiAccountSummary {
  id: string;
  userId: string;
  accountNumber: string;
  balance: number;
  currency: string;
}

/**
 * Respuesta DTO de presupuesto por categoría.
 */
export interface ApiBudgetResponse {
  id: string;
  userId: string;
  category: string;
  monthlyLimit: number;
  accumulatedSpend: number;
  percentageUsed: number;
}

/**
 * Respuesta DTO de movimiento contable.
 */
export interface ApiTransactionResponse {
  id: string;
  amount: number;
  currency: string;
  merchant: string;
  status: "PENDING" | "PROCESSED" | "QUARANTINED" | "CLEAN" | "SUSPICIOUS";
  category: string;
  timestamp: string;
}

/**
 * Ejecuta una petición HTTP genérica hacia la pasarela BFF.
 */
async function fetchApi<T>(input: RequestInfo | URL, init?: RequestInit): Promise<T> {
  const res = await fetch(input, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...init?.headers,
    },
  });

  if (!res.ok) {
    let errorMessage = `Error HTTP ${res.status}: ${res.statusText}`;
    try {
      const errorJson = await res.json();
      if (errorJson.message) errorMessage = errorJson.message;
    } catch {}
    throw new Error(errorMessage);
  }

  return res.json();
}

/**
 * Cliente de API tipado para comunicarse con la pasarela BFF de FinTrack.
 */
export const finTrackApi = {
  /**
   * Obtiene el resumen de la cuenta bancaria.
   */
  async getAccountSummary(): Promise<ApiAccountSummary> {
    return fetchApi<ApiAccountSummary>("/api/v1/accounts/summary");
  },

  /**
   * Obtiene la lista de presupuestos por categoría.
   */
  async getBudgets(): Promise<ApiBudgetResponse[]> {
    return fetchApi<ApiBudgetResponse[]>("/api/v1/budgets");
  },

  /**
   * Obtiene el historial de movimientos financieros.
   */
  async getTransactions(): Promise<ApiTransactionResponse[]> {
    return fetchApi<ApiTransactionResponse[]>("/api/v1/transactions");
  },

  /**
   * Crea o actualiza un presupuesto mensual por categoría.
   */
  async createBudget(category: string, monthlyLimit: number): Promise<ApiBudgetResponse> {
    return fetchApi<ApiBudgetResponse>("/api/v1/budgets", {
      method: "POST",
      body: JSON.stringify({
        category: category.toUpperCase().replace(/\s+/g, "_"),
        monthlyLimit,
      }),
    });
  },

  /**
   * Elimina un presupuesto por su ID.
   */
  async deleteBudget(id: string): Promise<void> {
    await fetchApi<void>(`/api/v1/budgets/${id}`, { method: "DELETE" });
  },

  /**
   * Envía un extracto bancario CSV a ingesta.
   */
  async uploadCsvStatement(file: File): Promise<{ success: boolean; message: string }> {
    const formData = new FormData();
    formData.append("file", file);

    const res = await fetch("/api/v1/ingest/csv", {
      method: "POST",
      body: formData,
    });
    return res.json();
  },

  /**
   * Envía un gasto manual a banking-ingest-service para clasificación asíncrona.
   */
  async ingestManualTransaction(
    merchant: string,
    amount: number
  ): Promise<{ transactionId: string; status: string; merchant: string; amount: number }> {
    return fetchApi<{ transactionId: string; status: string; merchant: string; amount: number }>(
      "/api/v1/ingest/transaction",
      {
        method: "POST",
        body: JSON.stringify({
          merchant,
          amount,
          currency: "EUR",
        }),
      }
    );
  },
};
