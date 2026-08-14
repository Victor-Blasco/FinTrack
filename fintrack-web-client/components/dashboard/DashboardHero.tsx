import React from "react";
import { Wallet, TrendingUp, ArrowUpRight, Plus, FileSpreadsheet } from "lucide-react";
import { BudgetCategory } from "@/types/dashboard";

interface DashboardHeroProps {
  accountBalance: number;
  budgets: BudgetCategory[];
  onAddExpense: () => void;
  onImportCsv: () => void;
}

/**
 * Componente Hero Card unificado para mostrar balance total, gasto mensual y accesos directos.
 */
export default function DashboardHero({
  accountBalance,
  budgets,
  onAddExpense,
  onImportCsv,
}: DashboardHeroProps) {
  const totalBudgetSpent = budgets.reduce((acc, curr) => acc + curr.spent, 0);
  const totalBudgetLimit = budgets.reduce((acc, curr) => acc + curr.limit, 0);
  const globalBudgetPercent =
    totalBudgetLimit > 0
      ? Math.min(Math.round((totalBudgetSpent / totalBudgetLimit) * 100), 100)
      : 0;

  return (
    <section className="mb-8">
      <div className="ui-card relative overflow-hidden bg-gradient-to-br from-card via-card to-accent/30 p-5 sm:p-6 border border-border rounded-2xl shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          
          {/* Saldo y Gasto Mensual */}
          <div>
            <div className="flex items-center gap-2 text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              <Wallet className="h-4 w-4 text-primary" />
              <span>Balance Total Disponible</span>
            </div>
            <div className="mt-1 text-3xl sm:text-4xl font-bold num tracking-tight text-foreground">
              {accountBalance.toLocaleString("es-ES", { minimumFractionDigits: 2, maximumFractionDigits: 2 })} €
            </div>
            
            <div className="mt-2 flex items-center gap-2 text-xs text-emerald-600 dark:text-emerald-400 font-medium">
              <span className="inline-flex items-center gap-0.5 rounded-md bg-emerald-500/10 px-2 py-0.5 font-bold">
                <ArrowUpRight className="h-3.5 w-3.5" /> +12% este mes
              </span>
              <span className="text-muted-foreground">• Saldo saludable</span>
            </div>
          </div>

          {/* Gasto Mensual & Barra Global */}
          <div className="w-full sm:w-64 rounded-xl bg-background/80 p-3.5 border border-border/80 backdrop-blur-xs">
            <div className="flex items-center justify-between text-xs font-semibold">
              <span className="text-muted-foreground flex items-center gap-1">
                <TrendingUp className="h-3.5 w-3.5 text-primary" /> Gasto del Mes
              </span>
              <span className="num font-bold text-foreground">{globalBudgetPercent}%</span>
            </div>

            <div className="mt-1 text-lg font-bold num text-foreground">
              {totalBudgetSpent.toFixed(2)} €
              <span className="text-xs font-normal text-muted-foreground"> / {totalBudgetLimit.toFixed(2)} €</span>
            </div>

            {/* Barra de progreso unificada */}
            <div className="mt-2 h-2 w-full overflow-hidden rounded-full bg-muted">
              <div
                className={`h-full rounded-full transition-all duration-500 ${
                  globalBudgetPercent > 90
                    ? "bg-red-500"
                    : globalBudgetPercent > 75
                    ? "bg-amber-500"
                    : "bg-primary"
                }`}
                style={{ width: `${globalBudgetPercent}%` }}
              />
            </div>
          </div>
        </div>

        {/* Accesos Rápidos Principales */}
        <div className="mt-5 pt-4 border-t border-border/60 flex flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-2 w-full sm:w-auto">
            <button
              type="button"
              onClick={onAddExpense}
              className="flex-1 sm:flex-none flex items-center justify-center gap-1.5 rounded-xl bg-primary px-4 py-2.5 text-xs font-bold text-primary-foreground hover:opacity-90 active:scale-95 transition shadow-xs cursor-pointer"
            >
              <Plus className="h-4 w-4" />
              <span>Añadir Gasto</span>
            </button>
            <button
              type="button"
              onClick={onImportCsv}
              className="flex-1 sm:flex-none flex items-center justify-center gap-1.5 rounded-xl border border-border bg-card px-4 py-2.5 text-xs font-bold text-foreground hover:bg-accent active:scale-95 transition cursor-pointer"
            >
              <FileSpreadsheet className="h-4 w-4 text-emerald-600 dark:text-emerald-400" />
              <span>Importar CSV</span>
            </button>
          </div>

          <span className="text-[11px] text-muted-foreground font-medium hidden md:inline">
            Última sincronización en vivo
          </span>
        </div>
      </div>
    </section>
  );
}
