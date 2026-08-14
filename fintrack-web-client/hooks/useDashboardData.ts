"use client";

import { useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { finTrackApi } from "@/lib/api";
import { BudgetCategory, Transaction, DEFAULT_SYSTEM_BUDGETS } from "@/types/dashboard";

/**
 * Custom Hook para la gestión centralizada de datos financieros y sincronización del Dashboard.
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
          name: b.category,
          spent: b.accumulatedSpend,
          limit: b.monthlyLimit,
          icon: b.category === "ALIMENTACION" ? "ShoppingBag" : b.category === "VIVIENDA" ? "Home" : "MoreHorizontal",
          isCustom: !["ALIMENTACION", "VIVIENDA", "OCIO", "TRANSPORTE", "SALUD_DEPORTE"].includes(b.category),
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
          category: t.category,
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
