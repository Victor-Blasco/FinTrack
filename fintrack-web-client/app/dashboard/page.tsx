"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import ThemeToggle from "@/components/ThemeToggle";

interface BudgetCategory {
  id: string;
  name: string;
  spent: number;
  limit: number;
  color: string;
}

interface Transaction {
  id: string;
  merchant: string;
  date: string;
  amount: number;
  category: string;
  status: "CLEAN" | "SUSPICIOUS" | "PENDING";
}

export default function DashboardPage() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(true);
  const [showToast, setShowToast] = useState(true);
  const [alertDismissed, setAlertDismissed] = useState(false);
  const router = useRouter();

  // Presupuestos de ejemplo
  const budgets: BudgetCategory[] = [
    { id: "1", name: "Alimentación", spent: 340.50, limit: 450.00, color: "bg-blue-500" },
    { id: "2", name: "Vivienda & Servicios", spent: 850.00, limit: 900.00, color: "bg-amber-500" },
    { id: "3", name: "Ocio & Restauración", spent: 210.00, limit: 200.00, color: "bg-red-500" },
    { id: "4", name: "Transporte", spent: 85.20, limit: 150.00, color: "bg-emerald-500" },
    { id: "5", name: "Salud & Deporte", spent: 65.00, limit: 120.00, color: "bg-indigo-500" },
    { id: "6", name: "Otros Gastos", spent: 120.00, limit: 250.00, color: "bg-slate-500" },
  ];

  // Transacciones recientes de ejemplo
  const transactions: Transaction[] = [
    { id: "tx-101", merchant: "Mercadona Supermercados", date: "Hoy, 14:32", amount: -42.50, category: "Alimentación", status: "CLEAN" },
    { id: "tx-102", merchant: "Transferencia Desconocida (Overseas)", date: "Hoy, 11:15", amount: -850.00, category: "Otros", status: "SUSPICIOUS" },
    { id: "tx-103", merchant: "Gasolinera Repsol", date: "Ayer, 18:40", amount: -55.00, category: "Transporte", status: "CLEAN" },
    { id: "tx-104", merchant: "Nómina Mensual Empresa", date: "28 Jul, 09:00", amount: 2450.00, category: "Ingreso", status: "CLEAN" },
  ];

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

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("userEmail");
    router.push("/login");
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-main text-main font-sans">
        <div className="flex items-center gap-3">
          <svg className="h-6 w-6 animate-spin text-blue-600" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
          </svg>
          <span className="text-sm font-medium text-muted">Cargando panel de control...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-main text-main pb-16 pt-6">
      <div className="mx-auto max-w-6xl px-4 sm:px-6">

        {/* 1. Banner Contextual de Alerta Antifraude (FraudShield) */}
        {!alertDismissed && (
          <div className="mb-6 flex flex-wrap items-center justify-between gap-4 rounded-xl border border-amber-200 bg-amber-50/80 p-4 text-amber-900 shadow-xs dark:border-amber-900/50 dark:bg-amber-950/30 dark:text-amber-200">
            <div className="flex items-center gap-3">
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-amber-100 dark:bg-amber-900/60">
                <svg className="h-5 w-5 text-amber-700 dark:text-amber-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.75" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              </div>
              <div>
                <h4 className="text-sm font-semibold">Alerta FraudShield: Transacción en Cuarentena</h4>
                <p className="text-xs text-amber-800 dark:text-amber-300/80">
                  Se ha bloqueado preventivamente una transacción de <span className="font-mono-data font-bold">850,00 €</span> por alta frecuencia/patrón inusual.
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <button
                onClick={() => alert("Revisando movimiento tx-102 en cuarentena")}
                className="rounded-lg bg-amber-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-amber-700 transition"
              >
                Revisar Cuarentena
              </button>
              <button
                onClick={() => setAlertDismissed(true)}
                className="rounded-lg p-1.5 text-amber-700 hover:bg-amber-100 dark:text-amber-400 dark:hover:bg-amber-900/50 transition"
              >
                <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>
        )}

        {/* 2. Header de Navegación */}
        <header className="mb-8 flex items-center justify-between border-b border-border pb-5">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary text-primary-foreground font-semibold shadow-xs">
              FT
            </div>
            <div>
              <h1 className="text-lg font-bold tracking-tight text-foreground">FinTrack</h1>
              <p className="text-xs text-muted-foreground font-medium">Finanzas Personales & FraudShield</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <span id="user-email-display" className="text-sm font-semibold text-foreground">
              {email}
            </span>
            <ThemeToggle />
            <button
              id="logout-btn"
              onClick={handleLogout}
              className="rounded-lg border border-border bg-card px-3.5 py-1.5 text-xs font-semibold text-foreground hover:bg-accent transition"
            >
              Cerrar Sesión
            </button>
          </div>
        </header>

        {/* 3. Métrica de Balance General */}
        <div className="mb-8 grid gap-4 sm:grid-cols-3">
          <div className="ui-card">
            <span className="text-xs font-semibold text-muted-foreground">Balance Total Disponible</span>
            <div className="mt-1 text-2xl font-bold font-mono-data text-foreground">
              14.245,50 €
            </div>
            <div className="mt-1 flex items-center gap-1 text-xs text-emerald-600 dark:text-emerald-400 font-medium">
              <span>↑ +12% respecto al mes anterior</span>
            </div>
          </div>

          <div className="ui-card">
            <span className="text-xs font-semibold text-muted-foreground">Gasto Acumulado del Mes</span>
            <div className="mt-1 text-2xl font-bold font-mono-data text-foreground">
              1.670,70 €
            </div>
            <div className="mt-1 text-xs text-muted-foreground font-medium">
              Presupuesto límite total: <span className="font-mono-data text-foreground font-semibold">2.070,00 €</span>
            </div>
          </div>

          <div className="ui-card">
            <span className="text-xs font-semibold text-muted-foreground">Estado de Seguridad (BFF)</span>
            <div className="mt-1 flex items-center gap-2">
              <span className="h-2.5 w-2.5 rounded-full bg-emerald-500"></span>
              <span className="text-lg font-bold text-foreground">Protegido por Kafka</span>
            </div>
            <div className="mt-1 text-xs text-muted-foreground font-medium">
              Verificación antifraude &lt;50ms
            </div>
          </div>
        </div>

        {/* 4. ELEMENTO PROTAGONISTA: Grid de Tarjetas de Presupuestos por Categoría */}
        <section className="mb-10">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-base font-bold text-foreground">
              Control de Presupuestos y Categorías
            </h2>
            <span className="text-xs font-semibold text-muted-foreground">
              6 categorías activas
            </span>
          </div>

          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {budgets.map((b) => {
              const percent = Math.min(Math.round((b.spent / b.limit) * 100), 100);
              const isOver = b.spent > b.limit;
              const isNear = percent >= 80 && !isOver;

              return (
                <div key={b.id} className="ui-card flex flex-col justify-between">
                  <div>
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-sm font-bold text-foreground">{b.name}</span>
                      {isOver ? (
                        <span className="rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-bold text-red-700 dark:bg-red-950/60 dark:text-red-300">
                          Excedido
                        </span>
                      ) : isNear ? (
                        <span className="rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-bold text-amber-800 dark:bg-amber-950/60 dark:text-amber-300">
                          Cerca del límite
                        </span>
                      ) : (
                        <span className="rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-bold text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300">
                          OK
                        </span>
                      )}
                    </div>

                    <div className="mt-3 flex items-baseline justify-between text-xs">
                      <span className="text-muted-foreground font-semibold">Gastado</span>
                      <span className="font-mono-data font-bold text-foreground">
                        {b.spent.toFixed(2)} € <span className="text-muted-foreground font-normal">/ {b.limit.toFixed(2)} €</span>
                      </span>
                    </div>

                    {/* Barra de progreso limpia */}
                    <div className="mt-2.5 h-2 w-full overflow-hidden rounded-full bg-muted">
                      <div
                        className={`h-full rounded-full transition-all duration-300 ${
                          isOver ? "bg-red-500" : isNear ? "bg-amber-500" : "bg-primary"
                        }`}
                        style={{ width: `${percent}%` }}
                      ></div>
                    </div>
                  </div>

                  <div className="mt-4 pt-2 flex items-center justify-between text-xs text-muted-foreground font-medium border-t border-border">
                    <span>Uso del límite</span>
                    <span className="font-mono-data font-bold text-foreground">{percent}%</span>
                  </div>
                </div>
              );
            })}
          </div>
        </section>

        {/* 5. SECCIÓN SECUNDARIA: Historial de Transacciones & Importación CSV */}
        <div className="grid gap-6 lg:grid-cols-3">
          
          {/* Feed de Transacciones Recientes (2 columnas) */}
          <section className="lg:col-span-2">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-base font-bold text-foreground">
                Historial de Movimientos Recientes
              </h3>
              <span className="text-xs font-semibold text-muted-foreground">Últimos eventos</span>
            </div>

            <div className="ui-card divide-y divide-border p-0 overflow-hidden">
              {transactions.map((tx) => (
                <div key={tx.id} className="flex items-center justify-between p-4 hover:bg-accent transition">
                  <div className="flex items-center gap-3">
                    <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-muted text-foreground font-bold">
                      {tx.amount > 0 ? "↓" : "↑"}
                    </div>
                    <div>
                      <div className="text-sm font-bold text-foreground">{tx.merchant}</div>
                      <div className="text-xs text-muted-foreground font-medium">
                        {tx.date} • <span className="font-semibold text-foreground">{tx.category}</span>
                      </div>
                    </div>
                  </div>

                  <div className="text-right">
                    <div className={`text-sm font-bold font-mono-data ${tx.amount > 0 ? "text-emerald-600 dark:text-emerald-400" : "text-foreground"}`}>
                      {tx.amount > 0 ? `+${tx.amount.toFixed(2)} €` : `${tx.amount.toFixed(2)} €`}
                    </div>
                    <div className="mt-0.5">
                      {tx.status === "CLEAN" && (
                        <span className="inline-block rounded-md bg-emerald-100 px-2 py-0.5 text-[10px] font-bold text-emerald-800 dark:bg-emerald-950/40 dark:text-emerald-300">
                          CLEAN
                        </span>
                      )}
                      {tx.status === "SUSPICIOUS" && (
                        <span className="inline-block rounded-md bg-red-100 px-2 py-0.5 text-[10px] font-bold text-red-800 dark:bg-red-950/40 dark:text-red-300">
                          CUARENTENA
                        </span>
                      )}
                      {tx.status === "PENDING" && (
                        <span className="inline-block rounded-md bg-amber-100 px-2 py-0.5 text-[10px] font-bold text-amber-800 dark:bg-amber-950/40 dark:text-amber-300">
                          PROCESANDO
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </section>

          {/* Importación de Extracto Bancario CSV */}
          <section className="lg:col-span-1">
            <div className="mb-4">
              <h3 className="text-base font-bold text-foreground">
                Ingesta Bancaria
              </h3>
            </div>

            <div className="ui-card flex flex-col justify-between h-[calc(100%-2rem)]">
              <div>
                <p className="text-xs text-muted-foreground font-medium mb-4">
                  Sube tu extracto bancario en formato <code className="font-mono-data bg-muted px-1.5 py-0.5 rounded text-foreground font-bold">.csv</code> para que los microservicios procesen y clasifiquen automáticamente tus gastos.
                </p>

                {/* Dropzone limpia de arrastre de archivos */}
                <div
                  onClick={() => alert("Simulación: Seleccionando archivo CSV para subir al banking-ingest-service...")}
                  className="cursor-pointer flex flex-col items-center justify-center rounded-xl border-2 border-dashed border-border p-6 text-center hover:border-primary hover:bg-accent transition"
                >
                  <svg className="h-8 w-8 text-muted-foreground mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                  </svg>
                  <span className="text-xs font-bold text-foreground">Arrastra tu archivo CSV aquí</span>
                  <span className="mt-1 text-[11px] text-muted-foreground font-medium">o haz clic para explorar en tu equipo</span>
                </div>
              </div>

              <div className="mt-6 pt-4 border-t border-border text-[11px] text-muted-foreground font-medium">
                Soporta PSD2 Webhooks & formato estándar de entidades bancarias.
              </div>
            </div>
          </section>

        </div>
      </div>

      {/* 6. Toast flotante en esquina inferior derecha */}
      {showToast && (
        <div className="fixed bottom-5 right-5 z-50 flex items-center justify-between gap-3 rounded-xl border border-border bg-card p-4 shadow-lg">
          <div className="flex items-center gap-3">
            <span className="relative flex h-2.5 w-2.5">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75"></span>
              <span className="relative inline-flex h-2.5 w-2.5 rounded-full bg-emerald-500"></span>
            </span>
            <span className="text-xs font-semibold text-foreground">
              Conectado al bus de eventos Kafka
            </span>
          </div>
          <button
            onClick={() => setShowToast(false)}
            className="text-muted-foreground hover:text-foreground text-xs font-bold"
          >
            ✕
          </button>
        </div>
      )}
    </div>
  );
}
