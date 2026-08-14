import React, { useState } from "react";
import MobileBottomSheet from "@/components/MobileBottomSheet";
import { BudgetCategory } from "@/types/dashboard";
import { ToastType } from "@/components/ToastNotification";
import { finTrackApi } from "@/lib/api";

interface AddBudgetModalProps {
  isOpen: boolean;
  onClose: () => void;
  setBudgets: React.Dispatch<React.SetStateAction<BudgetCategory[]>>;
  setToast: (toast: { isOpen: boolean; message: string; type: ToastType }) => void;
}

/**
 * Modal Bottom Sheet para añadir una nueva categoría de presupuesto personalizada.
 */
export default function AddBudgetModal({
  isOpen,
  onClose,
  setBudgets,
  setToast,
}: AddBudgetModalProps) {
  const [newCategoryName, setNewCategoryName] = useState<string>("");
  const [newCategoryLimit, setNewCategoryLimit] = useState<string>("");

  const handleAddBudgetSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCategoryName || !newCategoryLimit) return;

    const parsedLimit = parseFloat(newCategoryLimit);
    if (isNaN(parsedLimit) || parsedLimit <= 0) return;

    let apiId = `b-${Date.now()}`;
    try {
      const created = await finTrackApi.createBudget(newCategoryName, parsedLimit);
      if (created?.id) apiId = created.id;
    } catch (err) {
      console.warn("Creando presupuesto en modo de sincronización local:", err);
    }

    const newCategoryItem: BudgetCategory = {
      id: apiId,
      name: newCategoryName,
      spent: 0,
      limit: parsedLimit,
      icon: "MoreHorizontal",
      isCustom: true,
      subItems: [],
    };

    setBudgets((prev) => [...prev, newCategoryItem]);

    const createdName = newCategoryName;
    setNewCategoryName("");
    setNewCategoryLimit("");
    onClose();

    setToast({
      isOpen: true,
      message: `Presupuesto '${createdName}' creado correctamente`,
      type: "success",
    });
  };

  return (
    <MobileBottomSheet
      isOpen={isOpen}
      onClose={onClose}
      title="Nueva Categoría de Presupuesto"
    >
      <form onSubmit={handleAddBudgetSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-bold text-foreground mb-1">Nombre de la Categoría</label>
          <input
            type="text"
            required
            placeholder="Ej. Viajes, Mascotas, Regalos..."
            value={newCategoryName}
            onChange={(e) => setNewCategoryName(e.target.value)}
            className="w-full rounded-xl border border-border bg-background px-3.5 py-2.5 text-sm font-medium text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <div>
          <label className="block text-xs font-bold text-foreground mb-1">Límite Mensual (€)</label>
          <input
            type="number"
            step="0.01"
            required
            placeholder="300.00"
            value={newCategoryLimit}
            onChange={(e) => setNewCategoryLimit(e.target.value)}
            className="w-full rounded-xl border border-border bg-background px-3.5 py-2.5 text-sm font-bold num text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <button
          type="submit"
          className="w-full rounded-xl bg-primary py-3 text-xs font-bold text-primary-foreground hover:opacity-90 active:scale-98 transition shadow-xs cursor-pointer"
        >
          Crear Presupuesto
        </button>
      </form>
    </MobileBottomSheet>
  );
}
