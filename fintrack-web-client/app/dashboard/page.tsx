"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import ThemeToggle from "@/components/ThemeToggle";
import MobileBottomSheet from "@/components/MobileBottomSheet";
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
  const [showToast, setShowToast] = useState<boolean>(true);

  // Estado para formulario de nuevo gasto rápido
  const [newMerchant, setNewMerchant] = useState<string>("");
  const [newAmount, setNewAmount] = useState<string>("");
  const [newCategory, setNewCategory] = useState<string>("Alimentación");

  // Estado para formulario de nueva categoría de presupuesto
  const [newCategoryName, setNewCategoryName] = useState<string>("");
  const [newCategoryLimit, setNewCategoryLimit] = useState<string>("");

  // Presupuestos mock de ejemplo
  const [budgets, setBudgets] = useState<BudgetCategory[]>([
    {
      id: "b-1",
      name: "Alimentación",
      spent: 340.5,
      limit: 450.0,
      icon: "ShoppingBag",
      subItems: [
        { id: "sub-1", description: "Mercadona Supermercados", amount: 42.5, date: "Hoy" },
        { id: "sub-2", description: "Carrefour Express", amount: 88.0, date: "Hace 2 días" },
        { id: "sub-3", description: "Frutería de Barrio", amount: 15.0, date: "Hace 4 días" },
      ],
    },
    {
      id: "b-2",
      name: "Vivienda & Servicios",
      spent: 850.0,
      limit: 900.0,
      icon: "Home",
      subItems: [
        { id: "sub-4", description: "Alquiler Mensual", amount: 750.0, date: "01 Aug" },
        { id: "sub-5", description: "Factura Electricidad", amount: 100.0, date: "05 Aug" },
      ],
    },
    {
      id: "b-3",
      name: "Ocio & Restauración",
      spent: 210.0,
      limit: 200.0,
      icon: "Coffee",
      subItems: [
        { id: "sub-6", description: "Cena Restaurante", amount: 65.0, date: "Ayer" },
        { id: "sub-7", description: "Suscripción Streaming", amount: 15.0, date: "03 Aug" },
      ],
    },
    {
      id: "b-4",
      name: "Transporte",
      spent: 85.2,
      limit: 150.0,
      icon: "Car",
      subItems: [{ id: "sub-8", description: "Gasolinera Repsol", amount: 55.0, date: "Ayer" }],
    },
    {
      id: "b-5",
      name: "Salud & Deporte",
      spent: 65.0,
      limit: 120.0,
      icon: "HeartPulse",
      subItems: [{ id: "sub-9", description: "Cuota Gimnasio", amount: 45.0, date: "01 Aug" }],
    },
    {
      id: "b-6",
      name: "Otros Gastos",
      spent: 120.0,
      limit: 250.0,
      icon: "MoreHorizontal",
      subItems: [{ id: "sub-10", description: "Librería & Material", amount: 35.0, date: "04 Aug" }],
    },
  ]);

  // Transacciones mock de ejemplo
  const [transactions, setTransactions] = useState<Transaction[]>([
    {
      id: "tx-101",
      merchant: "Mercadona Supermercados",
      date: "Hoy, 14:32",
      amount: -42.5,
      category: "Alimentación",
      status: "CLEAN",
    },
    {
      id: "tx-102",
      merchant: "Transferencia Desconocida (Overseas)",
      date: "Hoy, 11:15",
      amount: -850.0,
      category: "Otros",
      status: "SUSPICIOUS",
    },
    {
      id: "tx-103",
      merchant: "Gasolinera Repsol",
      date: "Ayer, 18:40",
      amount: -55.0,
      category: "Transporte",
      status: "CLEAN",
    },
    {
      id: "tx-104",
      merchant: "Nómina Mensual Empresa",
      date: "28 Jul, 09:00",
      amount: 2450.0,
      category: "Ingreso",
      status: "CLEAN",
    },
    {
      id: "tx-105",
      merchant: "Restaurante La Tagliatella",
      date: "26 Jul, 21:15",
      amount: -65.0,
      category: "Ocio & Restauración",
      status: "CLEAN",
    },
  ]);

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      router.push("/login");
      return;
    }
    const storedEmail = localStorage.getItem("userEmail") || "user@fintrack.com";
    queueMicrotask(() => {
      setEmail(storedEmail);
      setLoading(false);
    });
  }, [router]);

  /**
   * Maneja el cierre de sesión eliminando las credenciales del almacenamiento local.
   */
  const handleLogout = () => {
    localStorage.removeItem("accessToken");
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
   * Registra un nuevo gasto ingresado desde el modal Bottom Sheet.
   *
   * @param e - Evento del formulario React
   */
  const handleAddExpenseSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMerchant || !newAmount) return;

    const parsedAmount = parseFloat(newAmount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) return;

    const newTx: Transaction = {
      id: `tx-${Date.now()}`,
      merchant: newMerchant,
      date: "Justo ahora",
      amount: -parsedAmount,
      category: newCategory,
      status: "CLEAN",
    };

    setTransactions((prev) => [newTx, ...prev]);

    // Actualizar el gastado acumulado de la categoría seleccionada
    setBudgets((prevBudgets) =>
      prevBudgets.map((b) => {
        if (b.name === newCategory) {
          return {
            ...b,
            spent: b.spent + parsedAmount,
            subItems: [
              ...(b.subItems || []),
              { id: `sub-${Date.now()}`, description: newMerchant, amount: parsedAmount, date: "Justo ahora" },
            ],
          };
        }
        return b;
      })
    );

    // Limpiar formulario y cerrar modal
    setNewMerchant("");
    setNewAmount("");
    setActiveModal("NONE");
  };

  /**
   * Registra una nueva categoría de presupuesto en el estado interactivo del cliente.
   *
   * @param e - Evento del formulario React
   */
  const handleAddBudgetSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCategoryName || !newCategoryLimit) return;

    const parsedLimit = parseFloat(newCategoryLimit);
    if (isNaN(parsedLimit) || parsedLimit <= 0) return;

    const newCategoryItem: BudgetCategory = {
      id: `b-${Date.now()}`,
      name: newCategoryName,
      spent: 0,
      limit: parsedLimit,
      icon: "MoreHorizontal",
      isCustom: true,
      subItems: [],
    };

    setBudgets((prev) => [...prev, newCategoryItem]);

    // Limpiar formulario y cerrar modal
    setNewCategoryName("");
    setNewCategoryLimit("");
    setActiveModal("NONE");
  };

  /**
   * Elimina una categoría de presupuesto personalizada creada por el usuario.
   * Las categorías predeterminadas del sistema son inmutables y no se pueden eliminar.
   *
   * @param budget - Categoría a eliminar
   * @param e - Evento del clic React
   */
  const handleDeleteBudget = (budget: BudgetCategory, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!budget.isCustom) {
      alert("Las categorías predeterminadas del sistema son inmutables y no se pueden eliminar.");
      return;
    }
    setBudgets((prev) => prev.filter((b) => b.id !== budget.id));
  };

  /**
   * Elimina un movimiento registrado del feed de transacciones.
   *
   * @param txId - Identificador único de la transacción a eliminar
   * @param e - Evento del clic React
   */
  const handleDeleteTransaction = (txId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    setTransactions((prev) => prev.filter((t) => t.id !== txId));
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
  const globalBudgetPercent = Math.min(
    Math.round((totalBudgetSpent / totalBudgetLimit) * 100),
    100
  );

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

  return (
    <div className="min-h-screen bg-background text-foreground pb-28 md:pb-12 pt-4 transition-colors">
      <div className="mx-auto max-w-5xl px-4 sm:px-6">

        {/* 1. Contextual FraudShield Alert Banner */}
        {!alertDismissed && (
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
                  Transacción de <span className="num font-bold">850,00 €</span> retenida en cuarentena por patrón poco habitual.
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
                  14.245,50 €
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
                            onClick={(e) => handleDeleteBudget(b, e)}
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
                      onClick={(e) => handleDeleteTransaction(tx.id, e)}
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

          <div>
            <label className="block text-xs font-bold text-foreground mb-1">Categoría</label>
            <select
              value={newCategory}
              onChange={(e) => setNewCategory(e.target.value)}
              className="w-full rounded-xl border border-border bg-background px-3.5 py-2.5 text-sm font-medium text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            >
              {budgets.map((b) => (
                <option key={b.id} value={b.name}>
                  {b.name}
                </option>
              ))}
            </select>
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
            onClick={() => {
              alert("Simulación de subida exitosa. Se han procesado 5 movimientos.");
              setActiveModal("NONE");
            }}
            className="cursor-pointer flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-border p-6 hover:border-primary hover:bg-accent/40 transition"
          >
            <FileSpreadsheet className="h-10 w-10 text-muted-foreground mb-2" />
            <span className="text-xs font-bold text-foreground">Arrastra tu archivo CSV aquí</span>
            <span className="mt-1 text-[11px] text-muted-foreground font-medium">o toca para seleccionar en tu teléfono</span>
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

      {/* 8. Toast flotante sutil de estado de red */}
      {showToast && (
        <div className="fixed bottom-16 sm:bottom-6 right-4 z-30 flex items-center justify-between gap-3 rounded-xl border border-border bg-card/90 backdrop-blur-md px-3.5 py-2.5 shadow-md">
          <div className="flex items-center gap-2">
            <span className="relative flex h-2 w-2">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75" />
              <span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-500" />
            </span>
            <span className="text-[11px] font-semibold text-foreground">
              Transacciones en tiempo real activas
            </span>
          </div>
          <button
            type="button"
            onClick={() => setShowToast(false)}
            className="text-muted-foreground hover:text-foreground text-xs font-bold cursor-pointer"
          >
            ✕
          </button>
        </div>
      )}
    </div>
  );
}
