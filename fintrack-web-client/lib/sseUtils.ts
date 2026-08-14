import { Transaction } from "@/types/dashboard";

/**
 * Nombres canónicos de eventos SSE emitidos por el backend.
 */
export const SSE_EVENT_NAMES = {
  TRANSACTION_UPDATE: "transaction-update",
  BALANCE_UPDATE: "balance-update",
  BUDGET_UPDATE: "budget-update",
  PING: "ping",
  INIT: "INIT",
} as const;

/**
 * Carga útil de un evento SSE recibido en streaming.
 */
export interface SseEventPayload {
  type?: string;
  event?: string;
  id?: string;
  transactionId?: string;
  merchant?: string;
  amount?: number;
  currency?: string;
  category?: string;
  status?: string;
  timestamp?: string;
  balance?: number;
  accountBalance?: number;
  accumulatedSpend?: number;
  monthlyLimit?: number;
  data?: SseEventPayload;
  payload?: SseEventPayload;
}

/**
 * Normaliza el nombre de categoría del backend a su formato legible en la interfaz de usuario.
 */
export function normalizeCategoryName(rawCategory?: string): string {
  if (!rawCategory) return "General";
  switch (rawCategory.toUpperCase()) {
    case "ALIMENTACION":
      return "Alimentación";
    case "VIVIENDA":
      return "Vivienda & Servicios";
    case "OCIO":
      return "Ocio & Restauración";
    case "TRANSPORTE":
      return "Transporte";
    case "SALUD_DEPORTE":
      return "Salud & Deporte";
    case "OTROS":
      return "Otros Gastos";
    default:
      return rawCategory;
  }
}

/**
 * Resuelve el estado de seguridad de una transacción a partir del payload del evento.
 */
export function resolveTransactionStatus(rawStatus?: string): "CLEAN" | "SUSPICIOUS" | "PENDING" {
  if (!rawStatus) return "PENDING";
  const upper = rawStatus.toUpperCase();
  if (upper.includes("SUSPICIOUS") || upper.includes("QUARANTINE") || upper.includes("FRAUD")) {
    return "SUSPICIOUS";
  }
  if (upper.includes("CLEAN") || upper.includes("PROCESSED")) {
    return "CLEAN";
  }
  return "PENDING";
}

/**
 * Transforma un payload SSE en un objeto Transaction fuertemente tipado.
 */
export function mapSseToTransaction(rawPayload: SseEventPayload): Transaction | null {
  const data = rawPayload.data || rawPayload.payload || rawPayload;
  const txId = data.id || data.transactionId;
  const hasTxFields = txId || data.merchant || typeof data.amount === "number";

  if (!hasTxFields) return null;

  return {
    id: txId || `tx-sse-${Date.now()}`,
    merchant: data.merchant || "Movimiento registrado",
    date: data.timestamp ? new Date(data.timestamp).toLocaleDateString("es-ES") : "Hoy",
    amount: typeof data.amount === "number" ? data.amount : 0,
    category: normalizeCategoryName(data.category),
    status: resolveTransactionStatus(data.status || data.type),
  };
}
