"use client";

import { useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { finTrackApi } from "@/lib/api";
import { BudgetCategory, Transaction, DEFAULT_SYSTEM_BUDGETS } from "@/types/dashboard";
import {
  SseEventPayload,
  SSE_EVENT_NAMES,
  normalizeCategoryName,
  mapSseToTransaction,
} from "@/lib/sseUtils";

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
        loadedBudgets = budgetsRes.value.map((b) => ({
          id: b.id,
          name: normalizeCategoryName(b.category),
          spent: b.accumulatedSpend,
          limit: b.monthlyLimit,
          icon: getCategoryIcon(b.category),
          isCustom: !["ALIMENTACION", "VIVIENDA", "OCIO", "TRANSPORTE", "SALUD_DEPORTE", "OTROS"].includes(
            (b.category || "").toUpperCase().replace(/\s+/g, "_")
          ),
          subItems: [],
        }));
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

    // 1. Actualización reactiva del saldo contable
    if (typeof data.balance === "number") {
      setAccountBalance(data.balance);
    } else if (typeof data.accountBalance === "number") {
      setAccountBalance(data.accountBalance);
    }

    // 2. Actualización reactiva de transacciones
    const incomingTx = mapSseToTransaction(rawPayload);
    if (incomingTx) {
      setTransactions((prevTxs) => {
        const existingIdx = prevTxs.findIndex(
          (t) =>
            t.id === incomingTx.id ||
            (t.merchant.toLowerCase() === incomingTx.merchant.toLowerCase() &&
              Math.abs(t.amount - incomingTx.amount) < 0.01 &&
              t.status === "PENDING")
        );

        if (existingIdx >= 0) {
          const updated = [...prevTxs];
          updated[existingIdx] = incomingTx;
          return updated;
        }
        return [incomingTx, ...prevTxs];
      });

      // Actualizar consumo acumulado en el presupuesto si es gasto limpio
      if (incomingTx.amount < 0 && incomingTx.status !== "SUSPICIOUS") {
        const absAmount = Math.abs(incomingTx.amount);
        setBudgets((prevBudgets) =>
          prevBudgets.map((b) => {
            const bName = b.name.toLowerCase();
            const catName = incomingTx.category.toLowerCase();
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

    // 3. Actualización reactiva de presupuestos
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
          // Mensaje de texto plano o heartbeat
        }
      };

      eventSource.onmessage = handleIncomingMessage;
      eventSource.addEventListener(SSE_EVENT_NAMES.TRANSACTION_UPDATE, handleIncomingMessage);
      eventSource.addEventListener(SSE_EVENT_NAMES.BALANCE_UPDATE, handleIncomingMessage);
      eventSource.addEventListener(SSE_EVENT_NAMES.BUDGET_UPDATE, handleIncomingMessage);
      eventSource.addEventListener(SSE_EVENT_NAMES.PING, () => {});

      eventSource.onerror = (err) => {
        console.debug("[FinTrack SSE] Estado de conexión streaming:", err);
      };
    } catch (err) {
      console.debug("[FinTrack SSE] Error de inicialización EventSource:", err);
    }

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
