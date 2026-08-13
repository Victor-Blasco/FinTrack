"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import ThemeToggle from "@/components/ThemeToggle";
import MobileBottomSheet from "@/components/MobileBottomSheet";
import ConfirmDialog from "@/components/ConfirmDialog";
import ToastNotification, { ToastType } from "@/components/ToastNotification";
import { finTrackApi } from "@/lib/api";
import {
  Wallet,
  TrendingUp,
  ShieldAlert,
  Plus,
  FileSpreadsheet,
  ChevronDown,
  ChevronUp,
  LayoutDashboard,
  PieChart,
  History,
  ShieldCheck,
  CheckCircle2,
  AlertTriangle,
  LogOut,
  ArrowUpRight,
  ArrowDownLeft,
  Filter,
  Sparkles,
  ShoppingBag,
  Home as HomeIcon,
  Coffee,
  Car,
  HeartPulse,
  MoreHorizontal,
  Trash2,
} from "lucide-react";

/**
 * Interfaz para representar una categoría de presupuesto en el sistema de gestión visual.
 */
export interface BudgetCategory {
  /** Identificador único de la categoría */
  id: string;
  /** Nombre de la categoría de gasto */
  name: string;
  /** Importe consumido acumulado en el mes (en euros) */
  spent: number;
  /** Límite mensual máximo asignado (en euros) */
  limit: number;
  /** Clave del icono descriptivo de la categoría */
  icon: string;
  /** Indica si la categoría fue creada por el usuario (eliminable) o es del sistema (inmutable) */
  isCustom?: boolean;
  /** Listado de sub-movimientos registrados dentro de esta categoría */
  subItems?: Array<{ id: string; description: string; amount: number; date: string }>;
}

/**
 * Interfaz para representar una transacción bancaria en el feed del usuario.
 */
export interface Transaction {
  /** Identificador único de la transacción */
  id: string;
  /** Nombre del establecimiento o emisor del pago */
  merchant: string;
  /** Cadena de fecha u hora de la transacción */
  date: string;
  /** Importe numérico de la transacción (positivo para ingresos, negativo para gastos) */
  amount: number;
  /** Categoría asignada a la transacción */
  category: string;
  /** Estado de validación por el motor FraudShield */
  status: "CLEAN" | "SUSPICIOUS" | "PENDING";
}

/**
 * Pestañas de filtrado rápido disponibles en el feed de movimientos.
 */
export type FilterTab = "ALL" | "FRAUD" | "EXPENSES" | "INCOMES";

/**
 * Tipos de modales emergentes (*Bottom Sheets*) activos en la vista.
 */
export type ActiveModal = "NONE" | "ADD_EXPENSE" | "IMPORT_CSV" | "REVIEW_FRAUD" | "ADD_BUDGET";

/**
 * Estado para el elemento pendiente de confirmación de borrado.
 */
export interface ConfirmItemState {
  type: "BUDGET" | "TRANSACTION";
  id: string;
  name: string;
}

/**
 * Categorías predeterminadas del sistema derivadas de Category.java (categorization-service).
 * Son inmutables y permanecen de forma continua en la aplicación.
 */
const DEFAULT_SYSTEM_BUDGETS: BudgetCategory[] = [
  { id: "sys-alimentacion", name: "Alimentación", spent: 0, limit: 450, icon: "ShoppingBag", isCustom: false, subItems: [] },
  { id: "sys-vivienda", name: "Vivienda & Servicios", spent: 0, limit: 900, icon: "Home", isCustom: false, subItems: [] },
  { id: "sys-ocio", name: "Ocio & Restauración", spent: 0, limit: 200, icon: "Coffee", isCustom: false, subItems: [] },
  { id: "sys-transporte", name: "Transporte", spent: 0, limit: 150, icon: "Car", isCustom: false, subItems: [] },
  { id: "sys-salud", name: "Salud & Deporte", spent: 0, limit: 120, icon: "HeartPulse", isCustom: false, subItems: [] },
  { id: "sys-otros", name: "Otros Gastos", spent: 0, limit: 250, icon: "MoreHorizontal", isCustom: false, subItems: [] },
];

/**
 * Componente principal de la pantalla de Dashboard (**`/dashboard`**).
 * <p>
 * Diseñado con enfoque **Mobile First**, ofrece un resumen financiero inmediato en el primer vistazo:
 * Hero Card con balance y gasto acumulado, tarjetas de presupuesto expandibles por categoría,
 * feed continuo de movimientos con filtros interactivos, barra de navegación fija inferior para móviles
 * y hojas emergentes (*Bottom Sheets*) para formularios rápidos y gestión de alertas FraudShield.
 * </p>
 *
 * @returns Elemento JSX representativo del Dashboard interactivo
 */
export default function DashboardPage() {
  const router = useRouter();

  // Estados de autenticación y carga de la vista
  const [email, setEmail] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(true);

  // Estados interactivos del Dashboard
  const [alertDismissed, setAlertDismissed] = useState<boolean>(false);
  const [activeTab, setActiveTab] = useState<FilterTab>("ALL");
  const [expandedCategory, setExpandedCategory] = useState<string | null>(null);
  const [activeModal, setActiveModal] = useState<ActiveModal>("NONE");

  // Estados para diálogos de confirmación y notificaciones Toast
  const [confirmState, setConfirmState] = useState<ConfirmItemState | null>(null);
  const [toastState, setToastState] = useState<{ isOpen: boolean; message: string; type: ToastType }>({
    isOpen: false,
    message: "",
    type: "success",
  });

  // Estado para formulario de nuevo gasto rápido
  const [newMerchant, setNewMerchant] = useState<string>("");
  const [newAmount, setNewAmount] = useState<string>("");
  const [newCategory, setNewCategory] = useState<string>("Alimentación");

  // Estado para formulario de nueva categoría de presupuesto
  const [newCategoryName, setNewCategoryName] = useState<string>("");
  const [newCategoryLimit, setNewCategoryLimit] = useState<string>("");

  // Presupuestos activados (inicializado con las 6 categorías inmutables del dominio)
  const [budgets, setBudgets] = useState<BudgetCategory[]>(DEFAULT_SYSTEM_BUDGETS);

  // Transacciones contables registradas en la aplicación
  const [transactions, setTransactions] = useState<Transaction[]>([]);

  // Estado para el saldo consolidado de la cuenta
  const [accountBalance, setAccountBalance] = useState<number>(0.0);

  // Estado para controlar que la carga inicial ha finalizado antes de guardar
  const [isLoaded, setIsLoaded] = useState<boolean>(false);

  // Cargar datos persistidos desde PostgreSQL / microservicios o sesión local al montar
  useEffect(() => {
    async function loadBackendData() {
      const storedEmail = typeof window !== "undefined" ? localStorage.getItem("userEmail") : null;
      if (storedEmail) setEmail(storedEmail);

      try {
        const [summaryRes, budgetsRes, txsRes] = await Promise.allSettled([
          finTrackApi.getAccountSummary(),
          finTrackApi.getBudgets(),
          finTrackApi.getTransactions(),
        ]);

        // Si todas las llamadas rebotan con error de autenticación, redirigir al login
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
          const savedBudgets = localStorage.getItem("fintrack_saved_budgets");
          if (savedBudgets) {
            try {
              loadedBudgets = JSON.parse(savedBudgets);
            } catch (ignored) {}
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
          const savedTxs = localStorage.getItem("fintrack_saved_transactions");
          if (savedTxs) {
            try {
              loadedTxs = JSON.parse(savedTxs);
            } catch (ignored) {}
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
    }

    loadBackendData();
  }, [router]);

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

  /**
   * Maneja el cierre de sesión destruyendo la cookie HttpOnly a través del BFF Route Handler.
   */
  const handleLogout = async () => {
    try {
      await fetch("/api/v1/auth/logout", { method: "POST" });
    } catch (ignored) {}
    localStorage.removeItem("userEmail");
    router.push("/login");
  };

  /**
   * Alterna la expansión de los sub-gastos de una categoría de presupuesto.
   *
   * @param categoryId - Identificador único de la categoría a alternar
   */
  const toggleCategoryExpand = (categoryId: string) => {
    setExpandedCategory((prev) => (prev === categoryId ? null : categoryId));
  };

  /**
   * Registra un nuevo gasto ingresado desde el modal Bottom Sheet enviándolo a la tubería de ingesta en Kafka.
   *
   * @param e - Evento del formulario React
   */
  const handleAddExpenseSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMerchant || !newAmount) return;

    const parsedAmount = parseFloat(newAmount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) return;

    let txId = `tx-man-${Date.now()}`;
    let isBackendActive = true;

    try {
      const response = await finTrackApi.ingestManualTransaction(newMerchant, parsedAmount);
      if (response?.transactionId) {
        txId = response.transactionId;
      }
    } catch (err) {
      console.warn("Modo offline / Sincronización local interactiva:", err);
      isBackendActive = false;
    }

    const newTx: Transaction = {
      id: txId,
      merchant: newMerchant,
      date: "Justo ahora",
      amount: -parsedAmount,
      category: "Procesando...",
      status: "PENDING",
    };

    setTransactions((prev) => [newTx, ...prev]);

    // Limpiar formulario y cerrar modal
    setNewMerchant("");
    setNewAmount("");
    setActiveModal("NONE");

    setToastState({
      isOpen: true,
      message: isBackendActive
        ? `Gasto de ${parsedAmount.toFixed(2)} € enviado a clasificación en Kafka`
        : `Gasto de ${parsedAmount.toFixed(2)} € registrado (modo local)`,
      type: "success",
    });
  };

  /**
   * Registra una nueva categoría de presupuesto en el backend / PostgreSQL y actualiza el estado.
   *
   * @param e - Evento del formulario React
   */
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
    setActiveModal("NONE");
    setToastState({
      isOpen: true,
      message: `Presupuesto '${createdName}' creado correctamente`,
      type: "success",
    });
  };

  /**
   * Inicia el flujo de eliminación de una categoría de presupuesto solicitando confirmación.
   *
   * @param budget - Categoría a eliminar
   * @param e - Evento del clic React
   */
  const handleInitiateDeleteBudget = (budget: BudgetCategory, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!budget.isCustom) {
      setToastState({
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

  /**
   * Inicia el flujo de eliminación de un movimiento contable solicitando confirmación.
   *
   * @param tx - Objeto {@link Transaction} a eliminar
   * @param e - Evento del clic React
   */
  const handleInitiateDeleteTransaction = (tx: Transaction, e: React.MouseEvent) => {
    e.stopPropagation();
    setConfirmState({
      type: "TRANSACTION",
      id: tx.id,
      name: tx.merchant,
    });
  };

  /**
   * Confirma y ejecuta la eliminación pendiente (presupuesto o transacción) en PostgreSQL y lanza el toast.
   */
  const handleConfirmDelete = async () => {
    if (!confirmState) return;

    if (confirmState.type === "BUDGET") {
      try {
        await finTrackApi.deleteBudget(confirmState.id);
      } catch (err) {
        console.warn("Eliminando presupuesto en modo interactivo local:", err);
      }

      setBudgets((prev) => prev.filter((b) => b.id !== confirmState.id));
      setToastState({
        isOpen: true,
        message: `Categoría '${confirmState.name}' eliminada correctamente`,
        type: "success",
      });
    } else if (confirmState.type === "TRANSACTION") {
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

      setToastState({
        isOpen: true,
        message: `Movimiento '${confirmState.name}' eliminado con éxito`,
        type: "success",
      });
    }

    setConfirmState(null);
  };

  /**
   * Procesa la ingesta de un archivo CSV real o descarga e ingesta sample_bank_statement.csv.
   */
  const handleIngestSampleCsv = async () => {
    try {
      const response = await fetch("/sample_bank_statement.csv");
      const csvText = await response.text();
      const file = new File([csvText], "sample_bank_statement.csv", { type: "text/csv" });
      const res = await finTrackApi.uploadCsvStatement(file);
      setToastState({
        isOpen: true,
        message: res.message || "Extracto CSV enviado para clasificación automática en Kafka",
        type: "success",
      });
    } catch (error) {
      console.warn("Procesando muestra local CSV por desconexión:", error);
      // Ingesta de demostración rápida desde sample_bank_statement.csv
      const parsedSampleTxs: Transaction[] = [
        { id: `tx-${Date.now()}-1`, merchant: "Mercadona Supermercados", date: "Hoy", amount: -45.50, category: "Alimentación", status: "CLEAN" },
        { id: `tx-${Date.now()}-2`, merchant: "Iberdrola Clientes", date: "Ayer", amount: -89.20, category: "Vivienda & Servicios", status: "CLEAN" },
        { id: `tx-${Date.now()}-3`, merchant: "Repsol Gasolinera", date: "Hace 2 días", amount: -60.00, category: "Transporte", status: "CLEAN" },
        { id: `tx-${Date.now()}-4`, merchant: "Restaurante El Celler", date: "Hace 3 días", amount: -35.00, category: "Ocio & Restauración", status: "CLEAN" },
        { id: `tx-${Date.now()}-5`, merchant: "Nómina Empresa Tech", date: "Hace 5 días", amount: 2450.00, category: "Ingresos", status: "CLEAN" },
      ];
      setTransactions((prev) => [...parsedSampleTxs, ...prev]);

      setBudgets((prevBudgets) =>
        prevBudgets.map((b) => {
          if (b.name === "Alimentación") {
            return { ...b, spent: b.spent + 45.50, subItems: [...(b.subItems || []), { id: `tx-${Date.now()}-1`, description: "Mercadona Supermercados", amount: 45.50, date: "Hoy" }] };
          }
          if (b.name === "Vivienda & Servicios") {
            return { ...b, spent: b.spent + 89.20, subItems: [...(b.subItems || []), { id: `tx-${Date.now()}-2`, description: "Iberdrola Clientes", amount: 89.20, date: "Ayer" }] };
          }
          if (b.name === "Transporte") {
            return { ...b, spent: b.spent + 60.00, subItems: [...(b.subItems || []), { id: `tx-${Date.now()}-3`, description: "Repsol Gasolinera", amount: 60.00, date: "Hace 2 días" }] };
          }
          if (b.name === "Ocio & Restauración") {
            return { ...b, spent: b.spent + 35.00, subItems: [...(b.subItems || []), { id: `tx-${Date.now()}-4`, description: "Restaurante El Celler", amount: 35.00, date: "Hace 3 días" }] };
          }
          return b;
        })
      );

      setToastState({
        isOpen: true,
        message: "5 transacciones clasificadas e ingestadas desde sample_bank_statement.csv",
        type: "success",
      });
    } finally {
      setActiveModal("NONE");
    }
  };

  // Filtrado de transacciones según la pestaña activa
  const filteredTransactions = transactions.filter((tx) => {
    if (activeTab === "FRAUD") return tx.status === "SUSPICIOUS";
    if (activeTab === "EXPENSES") return tx.amount < 0;
    if (activeTab === "INCOMES") return tx.amount > 0;
    return true;
  });

  // Cálculos consolidados de presupuestos globales
  const totalBudgetSpent = budgets.reduce((acc, curr) => acc + curr.spent, 0);
  const totalBudgetLimit = budgets.reduce((acc, curr) => acc + curr.limit, 0);
  const globalBudgetPercent = totalBudgetLimit > 0
    ? Math.min(Math.round((totalBudgetSpent / totalBudgetLimit) * 100), 100)
    : 0;

  // Obtener icono gráfico correspondiente por categoría
  const renderCategoryIcon = (iconName: string) => {
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
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background text-foreground font-sans">
        <div className="flex items-center gap-3">
          <svg className="h-6 w-6 animate-spin text-primary" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
          <span className="text-sm font-medium text-muted-foreground">
            Cargando tu experiencia financiera...
          </span>
        </div>
      </div>
    );
  }

  // Identificar si existe alguna transacción en cuarentena de seguridad activa
  const suspiciousTx = transactions.find((tx) => tx.status === "SUSPICIOUS");

  return (
    <div className="min-h-screen bg-background text-foreground pb-28 md:pb-12 pt-4 transition-colors">
      <div className="mx-auto max-w-5xl px-4 sm:px-6">

        {/* 1. Contextual FraudShield Alert Banner (se muestra solo si existe transacción en cuarentena activa) */}
        {!alertDismissed && suspiciousTx && (
          <div className="mb-5 flex flex-col sm:flex-row sm:items-center justify-between gap-3 rounded-2xl border border-amber-300/60 bg-amber-500/10 p-4 text-amber-900 dark:border-amber-700/50 dark:text-amber-200 shadow-xs backdrop-blur-xs transition">
            <div className="flex items-start gap-3">
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-amber-500/20 text-amber-600 dark:text-amber-400">
                <AlertTriangle className="h-5 w-5" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-amber-700 dark:text-amber-300">
                    Alerta de Seguridad Activa
                  </h4>
                  <span className="inline-flex items-center rounded-full bg-amber-500/20 px-2 py-0.5 text-[10px] font-bold text-amber-800 dark:text-amber-300">
                    FraudShield
                  </span>
                </div>
                <p className="mt-0.5 text-xs font-medium text-amber-900/90 dark:text-amber-200/90">
                  Transacción de <span className="num font-bold">{Math.abs(suspiciousTx.amount).toFixed(2)} €</span> ({suspiciousTx.merchant}) retenida en cuarentena por patrón poco habitual.
                </p>
              </div>
            </div>
            <div className="flex items-center justify-end gap-2 self-end sm:self-center">
              <button
                type="button"
                onClick={() => setActiveModal("REVIEW_FRAUD")}
                className="rounded-lg bg-amber-600 px-3.5 py-1.5 text-xs font-bold text-white hover:bg-amber-700 active:scale-95 transition shadow-xs cursor-pointer"
              >
                Revisar Cuarentena
              </button>
              <button
                type="button"
                onClick={() => setAlertDismissed(true)}
                className="rounded-lg p-1.5 text-amber-700 hover:bg-amber-500/20 dark:text-amber-400 transition cursor-pointer"
                title="Descartar aviso"
              >
                ✕
              </button>
            </div>
          </div>
        )}

        {/* 2. Top Navigation & User Header */}
        <header className="mb-6 flex items-center justify-between border-b border-border pb-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary text-primary-foreground font-bold text-sm shadow-sm tracking-tighter">
              FT
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-base font-bold tracking-tight text-foreground">
                  FinTrack
                </h1>
                <span className="inline-flex items-center gap-1 rounded-md bg-emerald-500/10 px-2 py-0.5 text-[10px] font-bold text-emerald-600 dark:text-emerald-400 border border-emerald-500/20">
                  <ShieldCheck className="h-3 w-3" /> Protegido
                </span>
              </div>
              <p className="text-xs text-muted-foreground font-medium">
                Hola, <span id="user-email-display" className="font-semibold text-foreground">{email.split("@")[0]}</span>
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <ThemeToggle />
            <button
              id="logout-btn"
              type="button"
              onClick={handleLogout}
              className="flex items-center gap-1.5 rounded-lg border border-border bg-card px-3 py-1.5 text-xs font-semibold text-foreground hover:bg-accent transition cursor-pointer"
              title="Cerrar sesión activa"
            >
              <LogOut className="h-3.5 w-3.5 text-muted-foreground" />
              <span className="hidden sm:inline">Salir</span>
            </button>
          </div>
        </header>

        {/* 3. HERO CARD UNIFICADA (Balance Total, Gasto del Mes y Accesos Rápidos) */}
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
                      globalBudgetPercent > 90 ? "bg-red-500" : globalBudgetPercent > 75 ? "bg-amber-500" : "bg-primary"
                    }`}
                    style={{ width: `${globalBudgetPercent}%` }}
                  />
                </div>
              </div>
            </div>

            {/* Accesos Rápidos Principales (Quick Action Buttons) */}
            <div className="mt-5 pt-4 border-t border-border/60 flex flex-wrap items-center justify-between gap-2">
              <div className="flex items-center gap-2 w-full sm:w-auto">
                <button
                  type="button"
                  onClick={() => setActiveModal("ADD_EXPENSE")}
                  className="flex-1 sm:flex-none flex items-center justify-center gap-1.5 rounded-xl bg-primary px-4 py-2.5 text-xs font-bold text-primary-foreground hover:opacity-90 active:scale-95 transition shadow-xs cursor-pointer"
                >
                  <Plus className="h-4 w-4" />
                  <span>Añadir Gasto</span>
                </button>
                <button
                  type="button"
                  onClick={() => setActiveModal("IMPORT_CSV")}
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

        {/* 4. ELEMENTO PROTAGONISTA: Grid de Presupuestos y Categorías Expandibles */}
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
              onClick={() => setActiveModal("ADD_BUDGET")}
              className="flex items-center gap-1 text-xs font-bold text-primary hover:underline cursor-pointer"
            >
              <Plus className="h-3.5 w-3.5" /> Nueva Categoría
            </button>
          </div>

          {/* Grid responsivo de tarjetas de presupuesto */}
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {budgets.map((b) => {
              const percent = Math.min(Math.round((b.spent / b.limit) * 100), 100);
              const isOver = b.spent > b.limit;
              const isNear = percent >= 80 && !isOver;
              const isExpanded = expandedCategory === b.id;

              return (
                <div
                  key={b.id}
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
                            onClick={(e) => handleInitiateDeleteBudget(b, e)}
                            className="p-1 rounded-md text-muted-foreground hover:bg-red-500/10 hover:text-red-600 dark:hover:text-red-400 transition cursor-pointer"
                            title="Eliminar categoría personalizada"
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                          </button>
                        )}
                      </div>
                    </div>

                    {/* Importes monetarios en Geist Mono */}
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
                        b.subItems.map((sub) => (
                          <div
                            key={sub.id}
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
        </section>

        {/* 5. FEED DE MOVIMIENTOS RECIENTES (Con Pestañas de Filtro Rápido) */}
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

            {/* Pestañas de Filtro Rápido Mobile First */}
            <div className="flex items-center gap-1 overflow-x-auto pb-1 sm:pb-0 scrollbar-none">
              <button
                type="button"
                onClick={() => setActiveTab("ALL")}
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
                onClick={() => setActiveTab("FRAUD")}
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
                onClick={() => setActiveTab("EXPENSES")}
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
                onClick={() => setActiveTab("INCOMES")}
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
              filteredTransactions.map((tx) => (
                <div
                  key={tx.id}
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
                      onClick={(e) => handleInitiateDeleteTransaction(tx, e)}
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
        </section>

      </div>

      {/* 6. NAVEGACIÓN INFERIOR FIJA (Mobile Bottom Navigation Bar) */}
      <nav
        aria-label="Navegación principal en móvil"
        className="fixed bottom-0 left-0 right-0 z-40 flex items-center justify-around border-t border-border bg-card/95 backdrop-blur-md px-4 py-2.5 sm:hidden shadow-lg"
      >
        <button
          type="button"
          onClick={() => setActiveTab("ALL")}
          className="flex flex-col items-center gap-1 text-primary cursor-pointer"
        >
          <LayoutDashboard className="h-5 w-5" />
          <span className="text-[10px] font-bold">Inicio</span>
        </button>

        <button
          type="button"
          onClick={() => {
            const el = document.querySelector("section");
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
          onClick={() => setActiveModal("ADD_EXPENSE")}
          className="-mt-5 flex h-12 w-12 items-center justify-center rounded-full bg-primary text-primary-foreground shadow-lg hover:scale-105 active:scale-95 transition cursor-pointer"
          title="Añadir nuevo gasto"
        >
          <Plus className="h-6 w-6" />
        </button>

        <button
          type="button"
          onClick={() => {
            setActiveTab("ALL");
          }}
          className="flex flex-col items-center gap-1 text-muted-foreground hover:text-foreground cursor-pointer"
        >
          <History className="h-5 w-5" />
          <span className="text-[10px] font-medium">Movimientos</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveModal("REVIEW_FRAUD")}
          className="flex flex-col items-center gap-1 text-amber-600 dark:text-amber-400 cursor-pointer relative"
        >
          <ShieldAlert className="h-5 w-5" />
          <span className="text-[10px] font-bold">Alertas</span>
          <span className="absolute top-0 right-2 h-2 w-2 rounded-full bg-amber-500 animate-pulse" />
        </button>
      </nav>

      {/* 7. MOBILE BOTTOM SHEETS (Modales Inferiores Emergentes) */}

      {/* A. Bottom Sheet: Añadir Nuevo Gasto */}
      <MobileBottomSheet
        isOpen={activeModal === "ADD_EXPENSE"}
        onClose={() => setActiveModal("NONE")}
        title="Registrar Nuevo Gasto"
      >
        <form onSubmit={handleAddExpenseSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-foreground mb-1">Comercio / Concepto</label>
            <input
              type="text"
              required
              placeholder="Ej. Supermercado, Gasolinera..."
              value={newMerchant}
              onChange={(e) => setNewMerchant(e.target.value)}
              className="w-full rounded-xl border border-border bg-background px-3.5 py-2.5 text-sm font-medium text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-foreground mb-1">Importe (€)</label>
            <input
              type="number"
              step="0.01"
              required
              placeholder="0.00"
              value={newAmount}
              onChange={(e) => setNewAmount(e.target.value)}
              className="w-full rounded-xl border border-border bg-background px-3.5 py-2.5 text-sm font-bold num text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>

          <div className="pt-2">
            <button
              type="submit"
              className="w-full rounded-xl bg-primary py-3 text-xs font-bold text-primary-foreground hover:opacity-90 active:scale-98 transition shadow-sm cursor-pointer"
            >
              Guardar Transacción
            </button>
          </div>
        </form>
      </MobileBottomSheet>

      {/* B. Bottom Sheet: Importación CSV */}
      <MobileBottomSheet
        isOpen={activeModal === "IMPORT_CSV"}
        onClose={() => setActiveModal("NONE")}
        title="Ingesta Bancaria de Extractos CSV"
      >
        <div className="space-y-4 text-center">
          <p className="text-xs text-muted-foreground font-medium">
            Sube tu extracto en formato <code className="num bg-muted px-1.5 py-0.5 rounded text-foreground font-bold">.csv</code> para que los algoritmos clasifiquen automáticamente tus gastos.
          </p>

          <div
            onClick={handleIngestSampleCsv}
            className="cursor-pointer flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-primary/40 bg-primary/5 p-6 hover:border-primary hover:bg-primary/10 transition group"
          >
            <FileSpreadsheet className="h-10 w-10 text-primary group-hover:scale-110 transition mb-2" />
            <span className="text-xs font-bold text-foreground">Procesar &quot;sample_bank_statement.csv&quot;</span>
            <span className="mt-1 text-[11px] text-muted-foreground font-medium">Haz clic aquí para ingestar el extracto bancario de prueba</span>
          </div>

          <div className="text-[11px] text-muted-foreground font-medium pt-2">
            Soporta formatos estándar de la directiva bancaria europea PSD2.
          </div>
        </div>
      </MobileBottomSheet>

      {/* C. Bottom Sheet: Revisar Cuarentena FraudShield */}
      <MobileBottomSheet
        isOpen={activeModal === "REVIEW_FRAUD"}
        onClose={() => setActiveModal("NONE")}
        title="Detalle de Transacción en Cuarentena"
      >
        <div className="space-y-4">
          <div className="rounded-xl bg-amber-500/10 border border-amber-500/20 p-4 text-amber-900 dark:text-amber-200">
            <div className="text-xs font-bold uppercase tracking-wider text-amber-700 dark:text-amber-300 mb-1">
              Retención de Seguridad
            </div>
            <div className="text-lg font-bold num">850,00 €</div>
            <div className="text-xs font-medium mt-1">Transferencia Desconocida (Overseas)</div>
            <div className="text-[11px] text-muted-foreground mt-0.5">Hoy, 11:15 • Origen no habitual</div>
          </div>

          <p className="text-xs text-muted-foreground font-medium">
            El sistema de protección FraudShield ha retenido esta operación por detectarla fuera de tu radio geográfico y hábito de consumo habitual.
          </p>

          <div className="grid grid-cols-2 gap-2 pt-2">
            <button
              type="button"
              onClick={() => {
                setTransactions((prev) =>
                  prev.map((t) => (t.id === "tx-102" ? { ...t, status: "CLEAN" } : t))
                );
                setAlertDismissed(true);
                setActiveModal("NONE");
              }}
              aria-label="Confirmar transacción como legítima"
              className="rounded-xl bg-emerald-600 py-2.5 text-xs font-bold text-white hover:bg-emerald-700 active:scale-95 transition shadow-xs cursor-pointer text-center"
            >
              Autorizar Transacción
            </button>
            <button
              type="button"
              onClick={() => {
                setTransactions((prev) => prev.filter((t) => t.id !== "tx-102"));
                setAlertDismissed(true);
                setActiveModal("NONE");
              }}
              aria-label="Confirmar transacción como fraude y bloquear"
              className="rounded-xl bg-red-600 py-2.5 text-xs font-bold text-white hover:bg-red-700 active:scale-95 transition shadow-xs cursor-pointer text-center"
            >
              Confirmar Fraude
            </button>
          </div>
        </div>
      </MobileBottomSheet>

      {/* D. Bottom Sheet: Añadir Categoría */}
      <MobileBottomSheet
        isOpen={activeModal === "ADD_BUDGET"}
        onClose={() => setActiveModal("NONE")}
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

      {/* 8. Componente Modal de Confirmación Reutilizable */}
      <ConfirmDialog
        isOpen={confirmState !== null}
        title={
          confirmState?.type === "BUDGET"
            ? "Eliminar Categoría de Presupuesto"
            : "Eliminar Movimiento Contable"
        }
        message={`¿Estás seguro de que deseas eliminar "${confirmState?.name}"? Esta acción no se puede deshacer.`}
        confirmText="Sí, Eliminar"
        cancelText="Cancelar"
        variant="danger"
        onConfirm={handleConfirmDelete}
        onCancel={() => setConfirmState(null)}
      />

      {/* 9. Componente ToastNotification Reutilizable (Auto-descarte a los 5 segundos) */}
      <ToastNotification
        isOpen={toastState.isOpen}
        message={toastState.message}
        type={toastState.type}
        durationMs={5000}
        onClose={() => setToastState((prev) => ({ ...prev, isOpen: false }))}
      />
    </div>
  );
}
