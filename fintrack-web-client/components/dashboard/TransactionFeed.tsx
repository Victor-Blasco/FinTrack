import React, { useState } from "react";
import {
  History,
  ArrowUpRight,
  ArrowDownLeft,
  CheckCircle2,
  AlertTriangle,
  Trash2,
} from "lucide-react";
import { Transaction, FilterTab, BudgetCategory, ConfirmItemState } from "@/types/dashboard";
import { ToastType } from "@/components/ToastNotification";
import ConfirmDialog from "@/components/ConfirmDialog";

interface TransactionFeedProps {
  transactions: Transaction[];
  activeTab: FilterTab;
  onTabChange: (tab: FilterTab) => void;
  setTransactions: React.Dispatch<React.SetStateAction<Transaction[]>>;
  setBudgets: React.Dispatch<React.SetStateAction<BudgetCategory[]>>;
  setToast: (toast: { isOpen: boolean; message: string; type: ToastType }) => void;
}

/**
 * Feed de movimientos financieros con filtros por tipo (Todos, Alertas, Gastos, Ingresos).
 */
export default function TransactionFeed({
  transactions,
  activeTab,
  onTabChange,
  setTransactions,
  setBudgets,
  setToast,
}: TransactionFeedProps) {
  const [confirmState, setConfirmState] = useState<ConfirmItemState | null>(null);

  const filteredTransactions = transactions.filter((tx) => {
    if (activeTab === "FRAUD") return tx.status === "SUSPICIOUS";
    if (activeTab === "EXPENSES") return tx.amount < 0;
    if (activeTab === "INCOMES") return tx.amount > 0;
    return true;
  });

  const handleInitiateDelete = (tx: Transaction, e: React.MouseEvent) => {
    e.stopPropagation();
    setConfirmState({
      type: "TRANSACTION",
      id: tx.id,
      name: tx.merchant,
    });
  };

  const handleConfirmDelete = () => {
    if (!confirmState) return;
    const txToDelete = transactions.find((t) => t.id === confirmState.id);
    setTransactions((prev) => prev.filter((t) => t.id !== confirmState.id));

    if (txToDelete) {
      const expenseAmount = Math.abs(txToDelete.amount);
      setBudgets((prevBudgets) =>
        prevBudgets.map((b) => {
          if (b.name.toLowerCase() === txToDelete.category.toLowerCase()) {
            const updatedSpent = Math.max(0, b.spent - expenseAmount);
            const updatedSubItems = (b.subItems || []).filter(
              (sub) => sub.id !== txToDelete.id && sub.description !== txToDelete.merchant
            );
            return {
              ...b,
              spent: updatedSpent,
              subItems: updatedSubItems,
            };
          }
          return b;
        })
      );
    }

    setToast({
      isOpen: true,
      message: `Movimiento '${confirmState.name}' eliminado con éxito`,
      type: "success",
    });
    setConfirmState(null);
  };

  return (
    <section className="mb-10">
      <div className="mb-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h3 className="text-base font-bold tracking-tight text-foreground flex items-center gap-2">
            <History className="h-4 w-4 text-primary" />
            <span>Últimos Movimientos</span>
          </h3>
          <p className="text-xs text-muted-foreground font-medium">
            Historial contable sincronizado en tiempo real
          </p>
        </div>

        {/* Pestañas de Filtro Rápido */}
        <div className="flex items-center gap-1 overflow-x-auto pb-1 sm:pb-0 scrollbar-none">
          <button
            type="button"
            onClick={() => onTabChange("ALL")}
            className={`rounded-lg px-3 py-1.5 text-xs font-bold transition cursor-pointer whitespace-nowrap ${
              activeTab === "ALL"
                ? "bg-primary text-primary-foreground shadow-xs"
                : "bg-card text-muted-foreground hover:bg-accent border border-border"
            }`}
          >
            Todos ({transactions.length})
          </button>
          <button
            type="button"
            onClick={() => onTabChange("FRAUD")}
            className={`rounded-lg px-3 py-1.5 text-xs font-bold transition cursor-pointer whitespace-nowrap ${
              activeTab === "FRAUD"
                ? "bg-amber-600 text-white shadow-xs"
                : "bg-card text-muted-foreground hover:bg-accent border border-border"
            }`}
          >
            Alertas ({transactions.filter((t) => t.status === "SUSPICIOUS").length})
          </button>
          <button
            type="button"
            onClick={() => onTabChange("EXPENSES")}
            className={`rounded-lg px-3 py-1.5 text-xs font-bold transition cursor-pointer whitespace-nowrap ${
              activeTab === "EXPENSES"
                ? "bg-primary text-primary-foreground shadow-xs"
                : "bg-card text-muted-foreground hover:bg-accent border border-border"
            }`}
          >
            Gastos
          </button>
          <button
            type="button"
            onClick={() => onTabChange("INCOMES")}
            className={`rounded-lg px-3 py-1.5 text-xs font-bold transition cursor-pointer whitespace-nowrap ${
              activeTab === "INCOMES"
                ? "bg-emerald-600 text-white shadow-xs"
                : "bg-card text-muted-foreground hover:bg-accent border border-border"
            }`}
          >
            Ingresos
          </button>
        </div>
      </div>

      {/* Feed contenedor continuo */}
      <div className="ui-card divide-y divide-border p-0 overflow-hidden rounded-2xl">
        {filteredTransactions.length > 0 ? (
          filteredTransactions.map((tx, idx) => (
            <div
              key={tx.id ? `${tx.id}-${idx}` : `tx-${idx}`}
              className="flex items-center justify-between p-4 hover:bg-accent/50 transition cursor-pointer"
            >
              <div className="flex items-center gap-3">
                <div
                  className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl font-bold ${
                    tx.amount > 0
                      ? "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
                      : "bg-muted text-foreground"
                  }`}
                >
                  {tx.amount > 0 ? <ArrowDownLeft className="h-5 w-5" /> : <ArrowUpRight className="h-5 w-5" />}
                </div>
                <div>
                  <div className="text-sm font-bold text-foreground tracking-tight">
                    {tx.merchant}
                  </div>
                  <div className="text-xs text-muted-foreground font-medium">
                    {tx.date} • <span className="font-semibold text-foreground">{tx.category}</span>
                  </div>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="text-right">
                  <div
                    className={`text-sm font-bold num ${
                      tx.amount > 0 ? "text-emerald-600 dark:text-emerald-400" : "text-foreground"
                    }`}
                  >
                    {tx.amount > 0 ? `+${tx.amount.toFixed(2)} €` : `${tx.amount.toFixed(2)} €`}
                  </div>
                  <div className="mt-0.5">
                    {tx.status === "CLEAN" && (
                      <span className="inline-flex items-center gap-1 rounded-md bg-emerald-500/10 px-2 py-0.5 text-[10px] font-bold text-emerald-600 dark:text-emerald-400">
                        <CheckCircle2 className="h-3 w-3" /> CLEAN
                      </span>
                    )}
                    {tx.status === "SUSPICIOUS" && (
                      <span className="inline-flex items-center gap-1 rounded-md bg-red-500/10 px-2 py-0.5 text-[10px] font-bold text-red-600 dark:text-red-400">
                        <AlertTriangle className="h-3 w-3" /> CUARENTENA
                      </span>
                    )}
                    {tx.status === "PENDING" && (
                      <span className="inline-flex items-center gap-1 rounded-md bg-amber-500/10 px-2 py-0.5 text-[10px] font-bold text-amber-600 dark:text-amber-400">
                        PROCESANDO
                      </span>
                    )}
                  </div>
                </div>

                <button
                  type="button"
                  onClick={(e) => handleInitiateDelete(tx, e)}
                  className="p-1.5 rounded-lg text-muted-foreground hover:bg-red-500/10 hover:text-red-600 dark:hover:text-red-400 transition cursor-pointer"
                  title="Eliminar movimiento"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            </div>
          ))
        ) : (
          <div className="p-8 text-center text-muted-foreground">
            <p className="text-xs font-semibold">No se encontraron movimientos para el filtro seleccionado.</p>
          </div>
        )}
      </div>

      {/* Diálogo de Confirmación */}
      <ConfirmDialog
        isOpen={confirmState !== null}
        title="¿Eliminar movimiento?"
        message={`¿Estás seguro de que deseas eliminar el movimiento de '${confirmState?.name}'?`}
        confirmText="Eliminar"
        variant="danger"
        onConfirm={handleConfirmDelete}
        onCancel={() => setConfirmState(null)}
      />
    </section>
  );
}
