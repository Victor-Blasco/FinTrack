"use client";

import Link from "next/link";
import {
  ArrowRight,
  FileUp,
  ShieldCheck,
  Wallet,
  Zap,
  TriangleAlert,
  BadgeCheck,
  Star,
  Landmark,
  Check,
  Quote,
  Sparkles,
} from "lucide-react";
import ThemeToggle from "@/components/ThemeToggle";

/**
 * Componente de vista principal para la Landing Page pública de FinTrack.
 * <p>
 * Muestra el Hero de presentación, métricas destacadas de rendimiento,
 * vista previa del panel de control y resumen de arquitectura de seguridad.
 * </p>
 *
 * @returns elemento JSX de la landing page pública
 */

const metrics = [
  { value: "Al instante", label: "Alertas de cargos anómalos", highlight: "text-blue-600 dark:text-blue-400" },
  { value: "6", label: "Categorías automáticas", highlight: "text-emerald-600 dark:text-emerald-400" },
  { value: "0 €", label: "Coste de inicio", highlight: "text-indigo-600 dark:text-indigo-400" },
  { value: "2 min", label: "Conexión bancaria", highlight: "text-amber-600 dark:text-amber-400" },
];

const features = [
  {
    icon: Wallet,
    title: "Presupuestos claros",
    text: "Define límites mensuales por categoría y consulta en cualquier momento cuánto has gastado y cuánto te queda disponible.",
    badgeBg: "bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20",
    items: ["Límites personalizables", "Alertas antes de superarlos", "Reinicio mensual automático"],
  },
  {
    icon: ShieldCheck,
    title: "Protección antifraude",
    text: "Detecta pagos que no encajan con tu perfil de uso y los detiene para que tú valides si son legítimos antes de que se procesen.",
    badgeBg: "bg-red-500/10 text-red-600 dark:text-red-400 border-red-500/20",
    items: ["Bloqueo preventivo en <50ms", "Validación rápida por el usuario", "Aprendizaje continuo"],
  },
  {
    icon: FileUp,
    title: "Sincronización automática",
    text: "Conecta tu cuenta bancaria de forma segura o importa tu extracto en CSV. Nosotros clasificamos y organizamos todos los movimientos.",
    badgeBg: "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20",
    items: ["Conexión bancaria segura (PSD2)", "Importación por arrastre CSV", "Sin movimientos duplicados"],
  },
];

const budgets = [
  { name: "Alimentación", spent: "382,40", limit: "500,00", pct: 76, state: "Vas bien", tone: "income" },
  { name: "Vivienda", spent: "846,00", limit: "900,00", pct: 94, state: "Casi al tope", tone: "warning" },
  { name: "Ocio", spent: "200,00", limit: "200,00", pct: 100, state: "Te has pasado", tone: "fraud" },
  { name: "Transporte", spent: "54,90", limit: "150,00", pct: 37, state: "Vas bien", tone: "income" },
];

const toneClasses: Record<string, { bar: string; text: string; chip: string }> = {
  income: { bar: "bg-emerald-500", text: "text-emerald-600 dark:text-emerald-400", chip: "bg-emerald-500/10 text-emerald-700 dark:text-emerald-300 border border-emerald-500/20" },
  warning: { bar: "bg-amber-500", text: "text-amber-600 dark:text-amber-400", chip: "bg-amber-500/10 text-amber-700 dark:text-amber-300 border border-amber-500/20" },
  fraud: { bar: "bg-red-500", text: "text-red-600 dark:text-red-400", chip: "bg-red-500/10 text-red-700 dark:text-red-300 border border-red-500/20" },
};

const banks = ["BBVA", "Santander", "CaixaBank", "Revolut", "N26", "Sabadell"];

const certs = [
  { label: "Datos cifrados", note: "Nivel bancario AES-256" },
  { label: "Sin claves", note: "Acceso OAuth2 / SCA" },
  { label: "Control total", note: "Revoca cuando quieras" },
];

const steps = [
  {
    n: "01",
    title: "Conecta tu cuenta",
    text: "Autoriza el acceso desde tu banco o importa tu extracto CSV. En un par de minutos tendrás toda la información organizada.",
  },
  {
    n: "02",
    title: "Establece tus límites",
    text: "Configura cuánto quieres gastar cada mes en cada categoría. Puedes ajustarlos en cualquier momento.",
  },
  {
    n: "03",
    title: "Vigilamos por ti",
    text: "Si detectamos un movimiento que no encaja con tu perfil habitual, lo bloqueamos y te avisamos para que lo revises.",
  },
];

const rules = [
  { label: "El pago es desde otro país", score: "Sospechoso", tone: "fraud" },
  { label: "El importe no es habitual", score: "Raro", tone: "warning" },
  { label: "Es una tienda que no conoces", score: "Raro", tone: "warning" },
  { label: "Tu móvil de siempre", score: "Normal", tone: "income" },
];

const techSpecs = [
  { label: "Decisión antifraude", value: "< 50 ms (p95)" },
  { label: "Trazabilidad de eventos", value: "100% auditado en Kafka" },
  { label: "Acceso bancario", value: "PSD2 con SCA" },
  { label: "Ingesta", value: "CSV + webhooks" },
  { label: "Cifrado en reposo", value: "AES-256" },
  { label: "Certificación", value: "ISO 27001" },
];

const faqs = [
  {
    q: "¿Y si me frena un pago que sí era mío?",
    a: "No se cancela, solo se queda en espera. Te avisamos y con un toque lo apruebas: el pago sigue su curso y la próxima vez ya no te preguntamos.",
  },
  {
    q: "¿Tengo que darte las claves de mi banco?",
    a: "No. Autorizas el acceso desde la web de tu propio banco, o si lo prefieres no conectas nada y subes tú el extracto.",
  },
  {
    q: "¿Cuánto tarda en avisarme?",
    a: "Prácticamente al momento: el aviso te llega mientras el pago aún está en espera, no días después.",
  },
  {
    q: "¿Puedo llevarme mis datos o borrarlo todo?",
    a: "Sí. Descargas todo tu historial en un archivo y borras la cuenta desde tu perfil, sin llamar a nadie.",
  },
];

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-background relative overflow-hidden">
      {/* Background Decorative Glow (Fondo de gradiente suave) */}
      <div className="pointer-events-none absolute top-[-100px] left-1/2 -translate-x-1/2 h-[500px] w-[800px] rounded-full bg-gradient-to-b from-blue-500/15 via-indigo-500/5 to-transparent blur-3xl opacity-70"></div>

      {/* HEADER */}
      <header className="sticky top-0 z-40 border-b border-border bg-background/80 backdrop-blur-md">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-5">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-primary font-bold text-primary-foreground shadow-md shadow-primary/20">
              FT
            </div>
            <span className="text-base font-bold tracking-tight">FinTrack</span>
            <span className="hidden items-center gap-1.5 rounded-full border border-primary/20 bg-primary/10 px-2.5 py-0.5 text-[11px] font-semibold text-primary sm:inline-flex">
              <Sparkles size={11} /> Gestión + seguridad
            </span>
          </div>
          <div className="flex items-center gap-3">
            <ThemeToggle />
            <Link
              href="/login"
              className="hidden h-9 items-center rounded-lg px-3.5 text-xs font-semibold text-muted-foreground transition-colors hover:text-foreground sm:inline-flex"
            >
              Iniciar Sesión
            </Link>
            <Link
              href="/register"
              className="inline-flex h-9 items-center rounded-lg bg-primary px-4 text-xs font-semibold text-primary-foreground shadow-md shadow-primary/20 transition-all hover:bg-primary/90 hover:shadow-lg hover:shadow-primary/30"
            >
              Crear Cuenta
            </Link>
          </div>
        </div>
      </header>

      <main className="relative z-10">
        {/* HERO SECTION */}
        <section className="mx-auto max-w-6xl px-5 pt-16 pb-16 sm:pt-24 sm:pb-20">
          <div className="inline-flex items-center gap-2 rounded-full border border-border bg-card px-3.5 py-1.5 text-xs font-medium shadow-xs">
            <span className="relative flex h-2 w-2">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-amber-400 opacity-75"></span>
              <span className="relative inline-flex h-2 w-2 rounded-full bg-amber-500"></span>
            </span>
            <Zap size={13} className="text-warning" />
            <span className="text-muted-foreground">Control de gastos y alertas en tiempo real</span>
          </div>

          <h1 className="mt-6 max-w-4xl text-4xl font-extrabold leading-[1.1] tracking-tight text-foreground sm:text-5xl md:text-6xl">
            Tu dinero bajo control, sin sorpresas a final de mes
          </h1>

          <p className="mt-5 max-w-2xl text-base leading-relaxed text-muted-foreground sm:text-lg">
            FinTrack unifica el control total de tus presupuestos personales con el motor de seguridad <span className="font-semibold text-foreground">FraudShield</span>, que detecta cargos anómalos al instante para que puedas revisarlos antes de que se procesen.
          </p>

          <div className="mt-8 flex flex-wrap items-center gap-4">
            <Link
              href="/register"
              className="inline-flex h-12 items-center gap-2 rounded-xl bg-primary px-6 text-sm font-semibold text-primary-foreground shadow-lg shadow-primary/25 transition-all hover:scale-[1.02] hover:bg-primary/90"
            >
              Empezar gratis <ArrowRight size={16} />
            </Link>
            <Link
              href="/dashboard"
              className="inline-flex h-12 items-center gap-2 rounded-xl border border-border bg-card px-6 text-sm font-semibold transition-all hover:bg-accent hover:border-primary/30"
            >
              Ver cómo funciona
            </Link>
          </div>

          {/* MÉTRICAS DESTACADAS */}
          <dl className="mt-14 grid grid-cols-2 gap-4 sm:grid-cols-4">
            {metrics.map((m) => (
              <div key={m.label} className="ui-card flex flex-col justify-between">
                <dd className={`num text-2xl font-bold ${m.highlight}`}>{m.value}</dd>
                <dt className="mt-2 text-xs font-medium text-muted-foreground">{m.label}</dt>
              </div>
            ))}
          </dl>
        </section>

        {/* TRUST & BANCOS */}
        <section className="border-t border-border bg-surface/60 backdrop-blur-xs">
          <div className="mx-auto max-w-6xl px-5 py-12">
            <div className="flex flex-col gap-8 lg:flex-row lg:items-center lg:justify-between">
              <div>
                <p className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
                  Compatible con tu banco
                </p>
                <div className="mt-4 flex flex-wrap items-center gap-x-6 gap-y-3">
                  {banks.map((b) => (
                    <span
                      key={b}
                      className="flex items-center gap-2 text-sm font-semibold text-foreground/80 hover:text-primary transition-colors"
                    >
                      <Landmark size={15} className="text-primary" />
                      {b}
                    </span>
                  ))}
                </div>
              </div>

              <div className="flex flex-wrap gap-3">
                {certs.map((c) => (
                  <div
                    key={c.label}
                    className="flex items-center gap-2.5 rounded-xl border border-border bg-card px-3.5 py-2 shadow-2xs"
                  >
                    <BadgeCheck size={16} className="text-income shrink-0" />
                    <div>
                      <p className="text-xs font-semibold">{c.label}</p>
                      <p className="text-[11px] text-muted-foreground">{c.note}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* TESTIMONIO */}
            <figure className="mt-10 border-t border-border pt-8 lg:flex lg:items-center lg:gap-8">
              <blockquote className="flex-1">
                <Quote size={18} className="text-primary" />
                <p className="mt-3 max-w-2xl text-sm leading-relaxed text-foreground font-medium">
                  «Nos ayudó a detectar un cargo duplicado de un proveedor antes incluso de revisar el
                  extracto. Desde entonces, el presupuesto familiar ya no se descuadra a final de mes.»
                </p>
                <figcaption className="mt-3 text-xs font-medium text-muted-foreground">
                  Marta Ferrán · usuaria desde 2025
                </figcaption>
              </blockquote>
              <div className="mt-6 flex items-center gap-3 lg:mt-0">
                <div className="flex gap-1">
                  {[0, 1, 2, 3, 4].map((i) => (
                    <Star key={i} size={15} className="fill-amber-400 text-amber-400" />
                  ))}
                </div>
                <p className="num text-sm font-semibold">
                  4,8<span className="text-muted-foreground font-normal">/5 · 1.240 valoraciones</span>
                </p>
              </div>
            </figure>
          </div>
        </section>

        {/* FEATURES (CARACTERÍSTICAS) */}
        <section className="border-t border-border">
          <div className="mx-auto max-w-6xl px-5 py-16 sm:py-24">
            <div className="text-center max-w-2xl mx-auto mb-12">
              <h2 className="text-2xl font-bold tracking-tight sm:text-4xl">
                Control y seguridad en un solo lugar
              </h2>
              <p className="mt-3 text-sm text-muted-foreground leading-relaxed">
                Todo lo que necesitas para gestionar tu dinero y protegerte de cargos no autorizados con una interfaz visual limpia.
              </p>
            </div>

            <div className="grid gap-6 md:grid-cols-3">
              {features.map((f) => (
                <article
                  key={f.title}
                  className="ui-card flex flex-col justify-between"
                >
                  <div>
                    <div className={`mb-4 flex h-11 w-11 items-center justify-center rounded-xl border ${f.badgeBg}`}>
                      <f.icon size={22} />
                    </div>
                    <h3 className="text-lg font-bold text-foreground">{f.title}</h3>
                    <p className="mt-2 text-xs leading-relaxed text-muted-foreground">{f.text}</p>
                  </div>
                  <ul className="mt-6 space-y-2 border-t border-border pt-4">
                    {f.items.map((i) => (
                      <li key={i} className="flex items-center gap-2 text-xs text-muted-foreground">
                        <Check size={14} className="text-emerald-500 shrink-0" />
                        {i}
                      </li>
                    ))}
                  </ul>
                </article>
              ))}
            </div>
          </div>
        </section>

        {/* CÓMO FUNCIONA */}
        <section className="border-t border-border bg-surface/60">
          <div className="mx-auto max-w-6xl px-5 py-16 sm:py-20">
            <div className="text-center max-w-xl mx-auto mb-12">
              <h2 className="text-2xl font-bold tracking-tight sm:text-3xl">Cómo funciona</h2>
              <p className="mt-2 text-sm text-muted-foreground">
                Tres pasos sencillos para tomar el control total.
              </p>
            </div>

            <ol className="grid gap-6 md:grid-cols-3">
              {steps.map((s) => (
                <li key={s.n} className="ui-card flex flex-col justify-between">
                  <div>
                    <span className="inline-flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10 text-xs font-bold text-primary font-mono-data border border-primary/20">
                      {s.n}
                    </span>
                    <h3 className="mt-4 text-base font-bold text-foreground">{s.title}</h3>
                    <p className="mt-2 text-xs leading-relaxed text-muted-foreground">{s.text}</p>
                  </div>
                </li>
              ))}
            </ol>
          </div>
        </section>

        {/* DASHBOARD PREVIEW (MOCKUP VISUAL) */}
        <section className="border-t border-border">
          <div className="mx-auto max-w-6xl px-5 py-16 sm:py-24">
            <div className="text-center max-w-xl mx-auto mb-10">
              <h2 className="text-2xl font-bold tracking-tight sm:text-3xl">Tu panel de control</h2>
              <p className="mt-2 text-sm text-muted-foreground">
                Alertas prioritarias al inicio y tus presupuestos organizados. Todo visible de una ojeada.
              </p>
            </div>

            {/* Mockup estilo ventana de app */}
            <div className="overflow-hidden rounded-2xl border border-border bg-card shadow-2xl">
              {/* Barra de ventana estilo macOS */}
              <div className="flex items-center justify-between border-b border-border bg-muted/40 px-4 py-3">
                <div className="flex items-center gap-2">
                  <div className="h-3 w-3 rounded-full bg-red-400"></div>
                  <div className="h-3 w-3 rounded-full bg-amber-400"></div>
                  <div className="h-3 w-3 rounded-full bg-emerald-400"></div>
                  <span className="ml-2 text-xs font-semibold text-muted-foreground">FinTrack Dashboard — Resumen Mensual</span>
                </div>
                <span className="num text-xs font-medium text-muted-foreground">Julio 2026</span>
              </div>

              {/* Banner de alerta de fraude dentro del mockup */}
              <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border bg-amber-500/10 px-5 py-3.5 text-amber-700 dark:text-amber-300">
                <div className="flex items-center gap-2.5">
                  <span className="relative flex h-2.5 w-2.5">
                    <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-amber-400 opacity-75"></span>
                    <span className="relative inline-flex h-2.5 w-2.5 rounded-full bg-amber-500"></span>
                  </span>
                  <TriangleAlert size={16} className="text-amber-500" />
                  <p className="text-xs font-semibold">
                    Movimiento retenido en cuarentena a la espera de tu confirmación
                  </p>
                </div>
                <span className="num text-xs font-bold text-amber-600 dark:text-amber-400">−128,90 €</span>
              </div>

              {/* Motivos de alerta */}
              <div className="border-b border-border px-5 py-4 bg-surface/30">
                <div className="flex items-baseline justify-between mb-3">
                  <p className="text-[11px] font-bold uppercase tracking-wider text-muted-foreground">
                    Reglas antifraude evaluadas (&lt;50ms)
                  </p>
                  <span className="text-xs font-semibold text-red-500">Pendiente de validación</span>
                </div>
                <ul className="grid gap-2 sm:grid-cols-2">
                  {rules.map((r) => {
                    const t = toneClasses[r.tone];
                    return (
                      <li
                        key={r.label}
                        className="flex items-center justify-between gap-3 rounded-lg border border-border bg-card px-3.5 py-2 text-xs"
                      >
                        <span className="text-muted-foreground">{r.label}</span>
                        <span className={`font-semibold ${t.text}`}>{r.score}</span>
                      </li>
                    );
                  })}
                </ul>
              </div>

              {/* Grid de presupuestos dentro del mockup */}
              <div className="grid gap-px bg-border sm:grid-cols-2">
                {budgets.map((b) => {
                  const t = toneClasses[b.tone];
                  return (
                    <div key={b.name} className="bg-card p-5">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-bold text-foreground">{b.name}</span>
                        <span className={`rounded-full px-2.5 py-0.5 text-[11px] font-bold ${t.chip}`}>
                          {b.state}
                        </span>
                      </div>
                      <div className="mt-3 flex items-baseline justify-between">
                        <span className="num text-xl font-bold text-foreground">{b.spent} €</span>
                        <span className="num text-xs text-muted-foreground">de {b.limit} €</span>
                      </div>
                      <div className="mt-3 h-2 w-full overflow-hidden rounded-full bg-muted">
                        <div className={`h-full rounded-full transition-all ${t.bar}`} style={{ width: `${b.pct}%` }} />
                      </div>
                      <div className={`num mt-2 text-xs font-semibold ${t.text}`}>{b.pct}% utilizado</div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </section>

        {/* ESPECIFICACIONES TÉCNICAS */}
        <section className="border-t border-border bg-surface/60">
          <div className="mx-auto max-w-6xl px-5 py-16">
            <div className="mb-8">
              <p className="text-xs font-bold uppercase tracking-wider text-primary">
                Especificaciones técnicas
              </p>
              <h2 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">
                Tecnología de alta disponibilidad
              </h2>
              <p className="mt-2 max-w-xl text-xs text-muted-foreground leading-relaxed">
                Arquitectura de microservicios orientada a eventos (EDA) respaldada por Apache Kafka y PostgreSQL.
              </p>
            </div>
            <dl className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {techSpecs.map((s) => (
                <div key={s.label} className="ui-card">
                  <dt className="text-xs text-muted-foreground font-medium">{s.label}</dt>
                  <dd className="num mt-1 text-sm font-bold text-foreground">{s.value}</dd>
                </div>
              ))}
            </dl>
          </div>
        </section>

        {/* FAQ */}
        <section className="border-t border-border">
          <div className="mx-auto grid max-w-6xl gap-10 px-5 py-16 sm:py-24 lg:grid-cols-[0.8fr_1.2fr]">
            <div>
              <h2 className="text-2xl font-bold tracking-tight sm:text-3xl">Preguntas frecuentes</h2>
              <p className="mt-3 max-w-sm text-xs leading-relaxed text-muted-foreground">
                Respuestas a las dudas más comunes antes de comenzar.
              </p>
            </div>
            <dl className="divide-y divide-border border-y border-border">
              {faqs.map((f) => (
                <div key={f.q} className="py-5">
                  <dt className="text-sm font-bold text-foreground">{f.q}</dt>
                  <dd className="mt-2 text-xs leading-relaxed text-muted-foreground">{f.a}</dd>
                </div>
              ))}
            </dl>
          </div>
        </section>

        {/* FINAL CTA */}
        <section className="border-t border-border bg-surface/40">
          <div className="mx-auto max-w-6xl px-5 py-16 sm:py-20">
            <div className="ui-card bg-gradient-to-br from-card via-card to-primary/5 p-8 sm:p-12 relative overflow-hidden">
              <h2 className="max-w-xl text-2xl font-bold tracking-tight sm:text-4xl text-foreground">
                Empieza hoy con total tranquilidad financiera
              </h2>
              <p className="mt-3 max-w-xl text-xs leading-relaxed text-muted-foreground">
                Controla cada euro y protege tus cuentas de cargos no autorizados. Sin coste inicial.
              </p>
              <div className="mt-8 flex flex-wrap items-center gap-4">
                <Link
                  href="/register"
                  className="inline-flex h-12 items-center gap-2 rounded-xl bg-primary px-6 text-sm font-semibold text-primary-foreground shadow-lg shadow-primary/25 transition-all hover:scale-[1.02] hover:bg-primary/90"
                >
                  Crear mi cuenta <ArrowRight size={16} />
                </Link>
                <Link
                  href="/dashboard"
                  className="inline-flex h-12 items-center rounded-xl border border-border bg-card px-6 text-sm font-semibold transition-all hover:bg-accent"
                >
                  Ver panel demo
                </Link>
              </div>
              <ul className="mt-6 flex flex-wrap gap-x-6 gap-y-2">
                {[
                  "Sin tarjeta de crédito",
                  "Exporta tus datos cuando quieras",
                  "Revoca el acceso en cualquier momento",
                ].map((i) => (
                  <li key={i} className="flex items-center gap-2 text-xs font-medium text-muted-foreground">
                    <Check size={14} className="text-emerald-500 shrink-0" />
                    {i}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </section>
      </main>

      {/* FOOTER */}
      <footer className="border-t border-border bg-card">
        <div className="mx-auto max-w-6xl px-5 py-12">
          <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
            <div>
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-xs font-bold text-primary-foreground shadow-xs">
                  FT
                </div>
                <span className="text-sm font-bold tracking-tight">FinTrack &amp; FraudShield</span>
              </div>
              <p className="mt-3 max-w-xs text-xs leading-relaxed text-muted-foreground">
                Control de gastos y protección antifraude en tiempo real para tu día a día.
              </p>
            </div>
            {[
              { title: "Producto", links: ["Acceso Usuarios", "Panel", "Subir extracto", "Conectar banco"] },
              { title: "Legal", links: ["Aviso legal", "Privacidad", "Cookies", "Términos"] },
              { title: "Soporte", links: ["Centro de ayuda", "Contacto", "Estado del servicio", "Seguridad"] },
            ].map((col) => (
              <nav key={col.title}>
                <p className="text-xs font-bold uppercase tracking-wider text-foreground">
                  {col.title}
                </p>
                <ul className="mt-3 space-y-2">
                  {col.links.map((l) => (
                    <li key={l}>
                      <Link
                        href={l === "Acceso Usuarios" ? "/login" : l === "Panel" ? "/dashboard" : "#"}
                        className="text-xs text-muted-foreground transition-colors hover:text-foreground"
                      >
                        {l}
                      </Link>
                    </li>
                  ))}
                </ul>
              </nav>
            ))}
          </div>
          <p className="num mt-10 border-t border-border pt-6 text-xs text-muted-foreground">
            © 2026 FinTrack &amp; FraudShield Ecosystem
          </p>
        </div>
      </footer>
    </div>
  );
}
