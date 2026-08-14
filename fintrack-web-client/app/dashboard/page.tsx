"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { ShieldCheck, LogOut } from "lucide-react";
import ThemeToggle from "@/components/ThemeToggle";
import ToastNotification, { ToastType } from "@/components/ToastNotification";
import { useDashboardData } from "@/hooks/useDashboardData";
import DashboardHero from "@/components/dashboard/DashboardHero";
import BudgetGrid from "@/components/dashboard/BudgetGrid";
import TransactionFeed from "@/components/dashboard/TransactionFeed";
import FraudAlertBanner from "@/components/dashboard/FraudAlertBanner";
import MobileNavBar from "@/components/dashboard/MobileNavBar";
import AddExpenseModal from "@/components/dashboard/modals/AddExpenseModal";
import ImportCsvModal from "@/components/dashboard/modals/ImportCsvModal";
import ReviewFraudModal from "@/components/dashboard/modals/ReviewFraudModal";
import AddBudgetModal from "@/components/dashboard/modals/AddBudgetModal";
import { FilterTab, ActiveModal } from "@/types/dashboard";

/**
 * Componente orquestador principal de la pantalla de Dashboard (/dashboard).
 * Arquitectura modular y mobile-first con sincronización de estado desacoplada.
 */
export default function DashboardPage() {
  const router = useRouter();
  const {
    email,
    loading,
    accountBalance,
    setAccountBalance,
    budgets,
    setBudgets,
    transactions,
    setTransactions,
  } = useDashboardData();

  const [activeTab, setActiveTab] = useState<FilterTab>("ALL");
  const [activeModal, setActiveModal] = useState<ActiveModal>("NONE");
  const [alertDismissed, setAlertDismissed] = useState<boolean>(false);
  const [toastState, setToastState] = useState<{
    isOpen: boolean;
    message: string;
    type: ToastType;
  }>({
    isOpen: false,
    message: "",
    type: "success",
  });

  const handleLogout = async () => {
    try {
      await fetch("/api/v1/auth/logout", { method: "POST" });
    } catch {}
    localStorage.removeItem("userEmail");
    router.push("/login");
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

  const suspiciousTx = transactions.find((tx) => tx.status === "SUSPICIOUS");

  return (
    <div className="min-h-screen bg-background text-foreground pb-28 md:pb-12 pt-4 transition-colors">
      <div className="mx-auto max-w-5xl px-4 sm:px-6">
        
        {/* Banner de alerta FraudShield */}
        {!alertDismissed && suspiciousTx && (
          <FraudAlertBanner
            suspiciousTx={suspiciousTx}
            onReview={() => setActiveModal("REVIEW_FRAUD")}
            onDismiss={() => setAlertDismissed(true)}
          />
        )}

        {/* Cabecera y Perfil de Usuario */}
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
                Hola, <span id="user-email-display" className="font-semibold text-foreground">{email ? email.split("@")[0] : "Usuario"}</span>
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

        {/* Hero Card: Balance Total y Gasto Mensual */}
        <DashboardHero
          accountBalance={accountBalance}
          budgets={budgets}
          onAddExpense={() => setActiveModal("ADD_EXPENSE")}
          onImportCsv={() => setActiveModal("IMPORT_CSV")}
        />

        {/* Grid de Presupuestos por Categoría */}
        <BudgetGrid
          budgets={budgets}
          onAddBudget={() => setActiveModal("ADD_BUDGET")}
          setBudgets={setBudgets}
          setToast={setToastState}
        />

        {/* Feed de Movimientos */}
        <TransactionFeed
          transactions={transactions}
          activeTab={activeTab}
          onTabChange={setActiveTab}
          setTransactions={setTransactions}
          setBudgets={setBudgets}
          setToast={setToastState}
        />
      </div>

      {/* Navegación Móvil Inferior */}
      <MobileNavBar
        onTabSelect={setActiveTab}
        onAddExpense={() => setActiveModal("ADD_EXPENSE")}
        onReviewAlerts={() => setActiveModal("REVIEW_FRAUD")}
      />

      {/* Modales Bottom Sheet */}
      <AddExpenseModal
        isOpen={activeModal === "ADD_EXPENSE"}
        onClose={() => setActiveModal("NONE")}
        setTransactions={setTransactions}
        setAccountBalance={setAccountBalance}
        setToast={setToastState}
      />
      <ImportCsvModal
        isOpen={activeModal === "IMPORT_CSV"}
        onClose={() => setActiveModal("NONE")}
        setTransactions={setTransactions}
        setBudgets={setBudgets}
        setToast={setToastState}
      />
      <ReviewFraudModal
        isOpen={activeModal === "REVIEW_FRAUD"}
        onClose={() => setActiveModal("NONE")}
        suspiciousTx={suspiciousTx}
        setTransactions={setTransactions}
        setAlertDismissed={setAlertDismissed}
      />
      <AddBudgetModal
        isOpen={activeModal === "ADD_BUDGET"}
        onClose={() => setActiveModal("NONE")}
        setBudgets={setBudgets}
        setToast={setToastState}
      />

      {/* Notificación Toast */}
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
