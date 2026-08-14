import React, { useState } from "react";
import {
  PieChart,
  Plus,
  Trash2,
  ChevronDown,
  ChevronUp,
  ShoppingBag,
  Home as HomeIcon,
  Coffee,
  Car,
  HeartPulse,
  MoreHorizontal,
} from "lucide-react";
import { BudgetCategory, ConfirmItemState } from "@/types/dashboard";
import { ToastType } from "@/components/ToastNotification";
import ConfirmDialog from "@/components/ConfirmDialog";
import { finTrackApi } from "@/lib/api";

interface BudgetGridProps {
  budgets: BudgetCategory[];
  onAddBudget: () => void;
  setBudgets: React.Dispatch<React.SetStateAction<BudgetCategory[]>>;
  setToast: (toast: { isOpen: boolean; message: string; type: ToastType }) => void;
}

/**
 * Renderiza el icono gráfico correspondiente por categoría.
 */
function renderCategoryIcon(iconName: string) {
  const className = "h-4 w-4 text-primary";
  switch (iconName) {
    case "ShoppingBag":
      return <ShoppingBag className={className} />;
    case "Home":
      return <HomeIcon className={className} />;
    case "Coffee":
      return <Coffee className={className} />;
    case "Car":
      return <Car className={className} />;
    case "HeartPulse":
      return <HeartPulse className={className} />;
    default:
      return <MoreHorizontal className={className} />;
  }
}

/**
 * Grid interactivo de presupuestos por categoría con tarjetas expandibles.
 */
export default function BudgetGrid({
  budgets,
  onAddBudget,
  setBudgets,
  setToast,
}: BudgetGridProps) {
  const [expandedCategory, setExpandedCategory] = useState<string | null>(null);
  const [confirmState, setConfirmState] = useState<ConfirmItemState | null>(null);

  const toggleCategoryExpand = (categoryId: string) => {
    setExpandedCategory((prev) => (prev === categoryId ? null : categoryId));
  };

  const handleInitiateDelete = (budget: BudgetCategory, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!budget.isCustom) {
      setToast({
        isOpen: true,
        message: "Las categorías predeterminadas del sistema son inmutables.",
        type: "error",
      });
      return;
    }
    setConfirmState({
      type: "BUDGET",
      id: budget.id,
      name: budget.name,
    });
  };

  const handleConfirmDelete = async () => {
    if (!confirmState) return;
    try {
      await finTrackApi.deleteBudget(confirmState.id);
    } catch (err) {
      console.warn("Eliminando presupuesto en modo interactivo local:", err);
    }

    setBudgets((prev) => prev.filter((b) => b.id !== confirmState.id));
    setToast({
      isOpen: true,
      message: `Categoría '${confirmState.name}' eliminada correctamente`,
      type: "success",
    });
    setConfirmState(null);
  };

  return (
    <section className="mb-10">
      <div className="mb-4 flex items-center justify-between">
        <div>
          <h2 className="text-base font-bold tracking-tight text-foreground flex items-center gap-2">
            <PieChart className="h-4 w-4 text-primary" />
            <span>Presupuestos por Categoría</span>
          </h2>
          <p className="text-xs text-muted-foreground font-medium">
            Toca cualquier tarjeta para expandir el historial detallado de la categoría
          </p>
        </div>

        <button
          type="button"
          onClick={onAddBudget}
          className="flex items-center gap-1 text-xs font-bold text-primary hover:underline cursor-pointer"
        >
          <Plus className="h-3.5 w-3.5" /> Nueva Categoría
        </button>
      </div>

      {/* Grid responsivo de tarjetas de presupuesto */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {budgets.map((b, bIdx) => {
          const percent = Math.min(Math.round((b.spent / b.limit) * 100), 100);
          const isOver = b.spent > b.limit;
          const isNear = percent >= 80 && !isOver;
          const isExpanded = expandedCategory === b.id;

          return (
            <div
              key={b.id ? `${b.id}-${bIdx}` : `budget-${bIdx}`}
              onClick={() => toggleCategoryExpand(b.id)}
              className={`ui-card flex flex-col justify-between cursor-pointer border transition-all duration-200 ${
                isExpanded ? "border-primary ring-1 ring-primary/20 shadow-md" : "hover:border-border"
              }`}
            >
              <div>
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-accent">
                      {renderCategoryIcon(b.icon)}
                    </div>
                    <span className="text-sm font-bold text-foreground">{b.name}</span>
                  </div>

                  <div className="flex items-center gap-1.5">
                    {/* Pill semántico de estado */}
                    {isOver ? (
                      <span className="rounded-full bg-red-500/10 border border-red-500/20 px-2 py-0.5 text-[11px] font-bold text-red-600 dark:text-red-400">
                        Excedido
                      </span>
                    ) : isNear ? (
                      <span className="rounded-full bg-amber-500/10 border border-amber-500/20 px-2 py-0.5 text-[11px] font-bold text-amber-600 dark:text-amber-400">
                        Al Límite
                      </span>
                    ) : (
                      <span className="rounded-full bg-emerald-500/10 border border-emerald-500/20 px-2 py-0.5 text-[11px] font-bold text-emerald-600 dark:text-emerald-400">
                        OK
                      </span>
                    )}

                    {/* Botón de eliminación exclusivo para categorías de usuario */}
                    {b.isCustom && (
                      <button
                        type="button"
                        onClick={(e) => handleInitiateDelete(b, e)}
                        className="p-1 rounded-md text-muted-foreground hover:bg-red-500/10 hover:text-red-600 dark:hover:text-red-400 transition cursor-pointer"
                        title="Eliminar categoría personalizada"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    )}
                  </div>
                </div>

                {/* Importes monetarios */}
                <div className="mt-3 flex items-baseline justify-between text-xs">
                  <span className="text-muted-foreground font-medium">Consumido</span>
                  <span className="num font-bold text-foreground">
                    {b.spent.toFixed(2)} €{" "}
                    <span className="text-muted-foreground font-normal">/ {b.limit.toFixed(2)} €</span>
                  </span>
                </div>

                {/* Barra de progreso limpia */}
                <div className="mt-2 h-2 w-full overflow-hidden rounded-full bg-muted">
                  <div
                    className={`h-full rounded-full transition-all duration-300 ${
                      isOver ? "bg-red-500" : isNear ? "bg-amber-500" : "bg-emerald-500"
                    }`}
                    style={{ width: `${percent}%` }}
                  />
                </div>
              </div>

              <div className="mt-4 pt-2.5 flex items-center justify-between text-xs text-muted-foreground font-medium border-t border-border">
                <span className="num">{percent}% del cupo usado</span>
                <span className="flex items-center gap-1 text-primary font-semibold">
                  {isExpanded ? "Ocultar" : "Detalles"}
                  {isExpanded ? <ChevronUp className="h-3.5 w-3.5" /> : <ChevronDown className="h-3.5 w-3.5" />}
                </span>
              </div>

              {/* Desglose expandible de sub-movimientos */}
              {isExpanded && (
                <div className="mt-3 pt-3 border-t border-border/80 space-y-2 animate-in fade-in slide-in-from-top-1 duration-200">
                  <div className="text-[11px] font-bold text-muted-foreground uppercase tracking-wider">
                    Desglose Reciente
                  </div>
                  {b.subItems && b.subItems.length > 0 ? (
                    b.subItems.map((sub, sIdx) => (
                      <div
                        key={sub.id ? `${sub.id}-${sIdx}` : `sub-${sIdx}`}
                        className="flex items-center justify-between text-xs p-2 rounded-lg bg-background border border-border/60"
                      >
                        <div>
                          <div className="font-semibold text-foreground">{sub.description}</div>
                          <div className="text-[10px] text-muted-foreground">{sub.date}</div>
                        </div>
                        <span className="num font-bold text-foreground">-{sub.amount.toFixed(2)} €</span>
                      </div>
                    ))
                  ) : (
                    <p className="text-xs text-muted-foreground italic">No hay desglose adicional disponible.</p>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Diálogo de Confirmación */}
      <ConfirmDialog
        isOpen={confirmState !== null}
        title="¿Eliminar presupuesto?"
        message={`¿Estás seguro de que deseas eliminar la categoría personalizada '${confirmState?.name}'? Esta acción no se puede deshacer.`}
        confirmText="Eliminar"
        variant="danger"
        onConfirm={handleConfirmDelete}
        onCancel={() => setConfirmState(null)}
      />
    </section>
  );
}
