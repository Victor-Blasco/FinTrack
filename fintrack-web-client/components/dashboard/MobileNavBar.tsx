import React from "react";
import { LayoutDashboard, PieChart, Plus, History, ShieldAlert } from "lucide-react";
import { FilterTab } from "@/types/dashboard";

interface MobileNavBarProps {
  onTabSelect: (tab: FilterTab) => void;
  onAddExpense: () => void;
  onReviewAlerts: () => void;
}

/**
 * Barra de navegación fija inferior para dispositivos móviles.
 */
export default function MobileNavBar({
  onTabSelect,
  onAddExpense,
  onReviewAlerts,
}: MobileNavBarProps) {
  return (
    <nav
      aria-label="Navegación principal en móvil"
      className="fixed bottom-0 left-0 right-0 z-40 flex items-center justify-around border-t border-border bg-card/95 backdrop-blur-md px-4 py-2.5 sm:hidden shadow-lg"
    >
      <button
        type="button"
        onClick={() => {
          onTabSelect("ALL");
          window.scrollTo({ top: 0, behavior: "smooth" });
        }}
        className="flex flex-col items-center gap-1 text-primary cursor-pointer"
      >
        <LayoutDashboard className="h-5 w-5" />
        <span className="text-[10px] font-bold">Inicio</span>
      </button>

      <button
        type="button"
        onClick={() => {
          const el = document.querySelector("section:nth-of-type(2)");
          el?.scrollIntoView({ behavior: "smooth" });
        }}
        className="flex flex-col items-center gap-1 text-muted-foreground hover:text-foreground cursor-pointer"
      >
        <PieChart className="h-5 w-5" />
        <span className="text-[10px] font-medium">Presupuestos</span>
      </button>

      {/* Botón flotante central de acción rápida (+) */}
      <button
        type="button"
        onClick={onAddExpense}
        className="-mt-5 flex h-12 w-12 items-center justify-center rounded-full bg-primary text-primary-foreground shadow-lg hover:scale-105 active:scale-95 transition cursor-pointer"
        title="Añadir nuevo gasto"
      >
        <Plus className="h-6 w-6" />
      </button>

      <button
        type="button"
        onClick={() => {
          onTabSelect("ALL");
          const el = document.querySelector("section:nth-of-type(3)");
          el?.scrollIntoView({ behavior: "smooth" });
        }}
        className="flex flex-col items-center gap-1 text-muted-foreground hover:text-foreground cursor-pointer"
      >
        <History className="h-5 w-5" />
        <span className="text-[10px] font-medium">Movimientos</span>
      </button>

      <button
        type="button"
        onClick={onReviewAlerts}
        className="flex flex-col items-center gap-1 text-amber-600 dark:text-amber-400 hover:opacity-80 cursor-pointer"
      >
        <ShieldAlert className="h-5 w-5" />
        <span className="text-[10px] font-medium">Alertas</span>
      </button>
    </nav>
  );
}
