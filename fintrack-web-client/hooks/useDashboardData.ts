"use client";

import { useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { finTrackApi } from "@/lib/api";
import { BudgetCategory, Transaction, DEFAULT_SYSTEM_BUDGETS } from "@/types/dashboard";

interface SseEventPayload {
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

function normalizeCategoryName(rawCategory: string): string {
  const cat = (rawCategory || "").toUpperCase().replace(/\s+/g, "_");
  if (cat === "ALIMENTACION") return "Alimentación";
  if (cat === "VIVIENDA") return "Vivienda & Servicios";
  if (cat === "OCIO") return "Ocio & Restauración";
  if (cat === "TRANSPORTE") return "Transporte";
  if (cat === "SALUD_DEPORTE" || cat === "SALUD") return "Salud & Deporte";
  if (cat === "OTROS") return "Otros Gastos";
  return rawCategory;
}

function getCategoryIcon(rawCategory: string): string {
  const cat = (rawCategory || "").toUpperCase().replace(/\s+/g, "_");
  if (cat === "ALIMENTACION") return "ShoppingBag";
  if (cat === "VIVIENDA") return "Home";
  if (cat === "OCIO") return "Coffee";
  if (cat === "TRANSPORTE") return "Car";
  if (cat === "SALUD_DEPORTE" || cat === "SALUD") return "HeartPulse";
  return "MoreHorizontal";
}

/**
 * Custom Hook para la gestión centralizada de datos financieros y sincronización reactiva
 * en tiempo real mediante Server-Sent Events (SSE) hacia /api/v1/events/stream.
 *
 * @author Victor Blasco
 */
export function useDashboardData() {
  const router = useRouter();
  const [email, setEmail] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(true);
  const [isLoaded, setIsLoaded] = useState<boolean>(false);
  const [accountBalance, setAccountBalance] = useState<number>(0.0);
  const [budgets, setBudgets] = useState<BudgetCategory[]>(DEFAULT_SYSTEM_BUDGETS);
  const [transactions, setTransactions] = useState<Transaction[]>([]);

  const loadData = useCallback(async () => {
    const storedEmail = typeof window !== "undefined" ? localStorage.getItem("userEmail") : null;
    if (storedEmail) setEmail(storedEmail);

    try {
      const [summaryRes, budgetsRes, txsRes] = await Promise.allSettled([
        finTrackApi.getAccountSummary(),
        finTrackApi.getBudgets(),
        finTrackApi.getTransactions(),
      ]);

      const allUnauthorized = [summaryRes, budgetsRes, txsRes].every(
        (res) => res.status === "rejected" && res.reason?.message?.includes("401")
      );
      if (allUnauthorized) {
        router.push("/login");
        return;
      }

      if (summaryRes.status === "fulfilled" && summaryRes.value) {
        setAccountBalance(summaryRes.value.balance);
      }

      let loadedBudgets: BudgetCategory[] = [];
      if (budgetsRes.status === "fulfilled" && budgetsRes.value && budgetsRes.value.length > 0) {
        loadedBudgets = budgetsRes.value.map((b) => {
          const rawCat = (b.category || "").toUpperCase().replace(/\s+/g, "_");
          return {
            id: b.id,
            name: normalizeCategoryName(b.category),
            spent: b.accumulatedSpend,
            limit: b.monthlyLimit,
            icon: getCategoryIcon(b.category),
            isCustom: !["ALIMENTACION", "VIVIENDA", "OCIO", "TRANSPORTE", "SALUD_DEPORTE", "OTROS"].includes(rawCat),
            subItems: [],
          };
        });
      } else {
        const savedBudgets = typeof window !== "undefined" ? localStorage.getItem("fintrack_saved_budgets") : null;
        if (savedBudgets) {
          try {
            loadedBudgets = JSON.parse(savedBudgets);
          } catch {}
        }
      }

      if (loadedBudgets.length > 0) {
        setBudgets(loadedBudgets);
      } else {
        setBudgets(DEFAULT_SYSTEM_BUDGETS);
      }

      let loadedTxs: Transaction[] = [];
      if (txsRes.status === "fulfilled" && txsRes.value && txsRes.value.length > 0) {
        loadedTxs = txsRes.value.map((t) => ({
          id: t.id,
          merchant: t.merchant,
          date: t.timestamp ? new Date(t.timestamp).toLocaleDateString("es-ES") : "Hoy",
          amount: t.amount,
          category: normalizeCategoryName(t.category),
          status: t.status === "QUARANTINED" ? "SUSPICIOUS" : t.status === "PROCESSED" ? "CLEAN" : "PENDING",
        }));
      } else {
        const savedTxs = typeof window !== "undefined" ? localStorage.getItem("fintrack_saved_transactions") : null;
        if (savedTxs) {
          try {
            loadedTxs = JSON.parse(savedTxs);
          } catch {}
        }
      }

      if (loadedTxs.length > 0) {
        setTransactions(loadedTxs);
      }
    } catch (err) {
      console.warn("Recuperando datos persistidos de sesión local:", err);
    } finally {
      setIsLoaded(true);
      setLoading(false);
    }
  }, [router]);

  // Carga inicial de datos contables
  useEffect(() => {
    let isSubscribed = true;
    async function initialize() {
      if (isSubscribed) {
        await loadData();
      }
    }
    initialize();
    return () => {
      isSubscribed = false;
    };
  }, [loadData]);

  // Procesador reactivo de eventos en tiempo real recibidos vía SSE
  const processSseEvent = useCallback((rawPayload: SseEventPayload) => {
    if (!rawPayload) return;
    const data: SseEventPayload = rawPayload.data || rawPayload.payload || rawPayload;

    // 1. Actualización reactiva instantánea del saldo contable
    if (typeof data.balance === "number") {
      setAccountBalance(data.balance);
    } else if (typeof data.accountBalance === "number") {
      setAccountBalance(data.accountBalance);
    }

    // 2. Actualización reactiva instantánea de transacciones
    const txId = data.id || data.transactionId;
    const hasTxFields = txId || data.merchant || typeof data.amount === "number";

    if (hasTxFields) {
      const resolvedId = txId || `tx-sse-${Date.now()}`;
      const resolvedMerchant = data.merchant || "Movimiento registrado";
      const resolvedAmount = typeof data.amount === "number" ? data.amount : 0;
      const rawCategory = data.category || "General";
      const resolvedCategory = normalizeCategoryName(rawCategory);

      const rawStatus = (data.status || data.type || "PENDING").toUpperCase();
      const resolvedStatus: "CLEAN" | "SUSPICIOUS" | "PENDING" =
        rawStatus.includes("SUSPICIOUS") || rawStatus.includes("QUARANTINE") || rawStatus.includes("FRAUD")
          ? "SUSPICIOUS"
          : rawStatus.includes("CLEAN") || rawStatus.includes("PROCESSED")
          ? "CLEAN"
          : "PENDING";

      const resolvedDate = data.timestamp ? new Date(data.timestamp).toLocaleDateString("es-ES") : "Hoy";

      const incomingTx: Transaction = {
        id: resolvedId,
        merchant: resolvedMerchant,
        date: resolvedDate,
        amount: resolvedAmount,
        category: resolvedCategory,
        status: resolvedStatus,
      };

      setTransactions((prevTxs) => {
        const existingIdx = prevTxs.findIndex(
          (t) =>
            t.id === resolvedId ||
            (t.merchant.toLowerCase() === resolvedMerchant.toLowerCase() &&
              Math.abs(t.amount - resolvedAmount) < 0.01 &&
              t.status === "PENDING")
        );

        if (existingIdx >= 0) {
          const updated = [...prevTxs];
          updated[existingIdx] = {
            ...updated[existingIdx],
            id: resolvedId,
            merchant: resolvedMerchant,
            amount: resolvedAmount,
            category: resolvedCategory,
            status: resolvedStatus,
          };
          return updated;
        }
        return [incomingTx, ...prevTxs];
      });

      // Actualizar consumo acumulado en el presupuesto de la categoría si es gasto limpio
      if (resolvedAmount < 0 && resolvedStatus !== "SUSPICIOUS") {
        const absAmount = Math.abs(resolvedAmount);
        setBudgets((prevBudgets) =>
          prevBudgets.map((b) => {
            const bName = b.name.toLowerCase();
            const catName = resolvedCategory.toLowerCase();
            if (bName.includes(catName) || catName.includes(bName)) {
              return {
                ...b,
                spent: Number((b.spent + absAmount).toFixed(2)),
              };
            }
            return b;
          })
        );
      }
    }

    // 3. Actualización reactiva instantánea de límites y consumos de presupuestos
    if (data.category && (typeof data.accumulatedSpend === "number" || typeof data.monthlyLimit === "number")) {
      setBudgets((prevBudgets) =>
        prevBudgets.map((b) => {
          const bKey = b.name.toUpperCase().replace(/\s+/g, "_");
          const targetKey = (data.category || "").toUpperCase().replace(/\s+/g, "_");
          if (bKey.includes(targetKey) || targetKey.includes(bKey)) {
            return {
              ...b,
              spent: typeof data.accumulatedSpend === "number" ? data.accumulatedSpend : b.spent,
              limit: typeof data.monthlyLimit === "number" ? data.monthlyLimit : b.limit,
            };
          }
          return b;
        })
      );
    }
  }, []);

  // Conexión reactiva en tiempo real mediante Server-Sent Events (SSE)
  useEffect(() => {
    if (typeof window === "undefined") return;

    let eventSource: EventSource | null = null;
    let isMounted = true;

    try {
      eventSource = new EventSource("/api/v1/events/stream");

      const handleIncomingMessage = (event: MessageEvent) => {
        if (!isMounted || !event.data) return;
        try {
          const parsed = JSON.parse(event.data);
          processSseEvent(parsed);
        } catch {
          // Si el mensaje es texto plano o heartbeat
        }
      };

      // Escucha de eventos genéricos y nombrados
      eventSource.onmessage = handleIncomingMessage;
      eventSource.addEventListener("transaction", handleIncomingMessage);
      eventSource.addEventListener("balance", handleIncomingMessage);
      eventSource.addEventListener("budget", handleIncomingMessage);
      eventSource.addEventListener("fraud_alert", handleIncomingMessage);
      eventSource.addEventListener("ping", () => {});

      eventSource.onerror = (err) => {
        // En caso de corte o reconexión de red, EventSource gestiona el reintento automático
        console.debug("[FinTrack SSE] Notificación de estado de canal en streaming:", err);
      };
    } catch (err) {
      console.debug("[FinTrack SSE] Inicialización de EventSource:", err);
    }

    // Cierre limpio de la conexión en el desmontaje del efecto
    return () => {
      isMounted = false;
      if (eventSource) {
        eventSource.close();
        eventSource = null;
      }
    };
  }, [processSseEvent]);

  // Persistir presupuestos cuando cambian tras finalizar la carga inicial
  useEffect(() => {
    if (!isLoaded) return;
    try {
      localStorage.setItem("fintrack_saved_budgets", JSON.stringify(budgets));
    } catch (err) {
      console.error("Error al guardar presupuestos:", err);
    }
  }, [budgets, isLoaded]);

  // Persistir transacciones cuando cambian tras finalizar la carga inicial
  useEffect(() => {
    if (!isLoaded) return;
    try {
      localStorage.setItem("fintrack_saved_transactions", JSON.stringify(transactions));
    } catch (err) {
      console.error("Error al guardar transacciones:", err);
    }
  }, [transactions, isLoaded]);

  return {
    email,
    loading,
    accountBalance,
    setAccountBalance,
    budgets,
    setBudgets,
    transactions,
    setTransactions,
    reload: loadData,
  };
}
