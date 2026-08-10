/**
 * Módulo de cliente HTTP tipado para la interacción Full-Stack con los microservicios de FinTrack.
 * <p>
 * Se encarga de realizar las peticiones asíncronas hacia el middleware Proxy BFF (`/api/v1/`),
 * inyectando automáticamente la cabecera `Authorization: Bearer <token>` obtenida desde `localStorage`.
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
 * Función auxiliar interna para construir las cabeceras HTTP con token de autenticación.
 */
function getAuthHeaders(): HeadersInit {
  const token = typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  return headers;
}

/**
 * Cliente de API tipado para comunicarse con la arquitectura de microservicios de FinTrack.
 */
export const finTrackApi = {
  /**
   * Obtiene el resumen de la cuenta bancaria y saldo acumulado del usuario.
   *
   * @returns Promesa con los datos de {@link ApiAccountSummary}
   */
  async getAccountSummary(): Promise<ApiAccountSummary> {
    const res = await fetch("/api/v1/accounts/summary", {
      headers: getAuthHeaders(),
    });
    if (!res.ok) {
      throw new Error(`Error al obtener el resumen de cuenta: ${res.statusText}`);
    }
    return res.json();
  },

  /**
   * Obtiene la lista de presupuestos y consumos por categoría del usuario.
   *
   * @returns Promesa con el arreglo de {@link ApiBudgetResponse}
   */
  async getBudgets(): Promise<ApiBudgetResponse[]> {
    const res = await fetch("/api/v1/budgets", {
      headers: getAuthHeaders(),
    });
    if (!res.ok) {
      throw new Error(`Error al obtener presupuestos: ${res.statusText}`);
    }
    return res.json();
  },

  /**
   * Crea un nuevo presupuesto o actualiza el límite mensual existente para una categoría.
   *
   * @param category - Nombre de la categoría de gasto
   * @param monthlyLimit - Límite mensual configurado
   * @returns Promesa con la respuesta {@link ApiBudgetResponse}
   */
  async createBudget(category: string, monthlyLimit: number): Promise<ApiBudgetResponse> {
    const res = await fetch("/api/v1/budgets", {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({
        category: category.toUpperCase().replace(/\s+/g, "_"),
        monthlyLimit,
      }),
    });
    if (!res.ok) {
      throw new Error(`Error al crear presupuesto: ${res.statusText}`);
    }
    return res.json();
  },

  /**
   * Elimina una categoría de presupuesto personalizada por su ID.
   *
   * @param id - Identificador único del presupuesto a eliminar
   */
  async deleteBudget(id: string): Promise<void> {
    const res = await fetch(`/api/v1/budgets/${id}`, {
      method: "DELETE",
      headers: getAuthHeaders(),
    });
    if (!res.ok) {
      throw new Error(`Error al eliminar presupuesto: ${res.statusText}`);
    }
  },

  /**
   * Obtiene el historial ordenado de movimientos financieros del usuario.
   *
   * @returns Promesa con el arreglo de {@link ApiTransactionResponse}
   */
  async getTransactions(): Promise<ApiTransactionResponse[]> {
    const res = await fetch("/api/v1/transactions", {
      headers: getAuthHeaders(),
    });
    if (!res.ok) {
      throw new Error(`Error al obtener transacciones: ${res.statusText}`);
    }
    return res.json();
  },

  /**
   * Envía un extracto bancario CSV a `banking-ingest-service` para su procesamiento asíncrono en Kafka.
   *
   * @param file - Archivo CSV seleccionado
   * @returns Promesa indicando el resultado del envío
   */
  async uploadCsvStatement(file: File): Promise<{ success: boolean; message: string }> {
    const token = typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;
    const formData = new FormData();
    formData.append("file", file);

    const headers: Record<string, string> = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    const res = await fetch("/api/v1/ingest/csv", {
      method: "POST",
      headers,
      body: formData,
    });

    if (!res.ok) {
      throw new Error(`Error al subir extracto CSV: ${res.statusText}`);
    }

    return res.json();
  },
};
