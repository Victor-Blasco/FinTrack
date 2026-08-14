/**
 * Tipos e interfaces del dominio del Dashboard de FinTrack.
 */

export interface BudgetCategory {
  id: string;
  name: string;
  spent: number;
  limit: number;
  icon: string;
  isCustom?: boolean;
  subItems?: Array<{ id: string; description: string; amount: number; date: string }>;
}

export interface Transaction {
  id: string;
  merchant: string;
  date: string;
  amount: number;
  category: string;
  status: "CLEAN" | "SUSPICIOUS" | "PENDING";
}

export type FilterTab = "ALL" | "FRAUD" | "EXPENSES" | "INCOMES";

export type ActiveModal = "NONE" | "ADD_EXPENSE" | "IMPORT_CSV" | "REVIEW_FRAUD" | "ADD_BUDGET";

export interface ConfirmItemState {
  type: "BUDGET" | "TRANSACTION";
  id: string;
  name: string;
}

/**
 * Categorías predeterminadas del sistema derivadas de Category.java (categorization-service).
 */
export const DEFAULT_SYSTEM_BUDGETS: BudgetCategory[] = [
  { id: "sys-alimentacion", name: "Alimentación", spent: 0, limit: 450, icon: "ShoppingBag", isCustom: false, subItems: [] },
  { id: "sys-vivienda", name: "Vivienda & Servicios", spent: 0, limit: 900, icon: "Home", isCustom: false, subItems: [] },
  { id: "sys-ocio", name: "Ocio & Restauración", spent: 0, limit: 200, icon: "Coffee", isCustom: false, subItems: [] },
  { id: "sys-transporte", name: "Transporte", spent: 0, limit: 150, icon: "Car", isCustom: false, subItems: [] },
  { id: "sys-salud", name: "Salud & Deporte", spent: 0, limit: 120, icon: "HeartPulse", isCustom: false, subItems: [] },
  { id: "sys-otros", name: "Otros Gastos", spent: 0, limit: 250, icon: "MoreHorizontal", isCustom: false, subItems: [] },
];
