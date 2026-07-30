import { createFileRoute } from "@tanstack/react-router";
import {
  ArrowRight,
  FileUp,
  ShieldCheck,
  Wallet,
  Zap,
  Lock,
  TriangleAlert,
  BadgeCheck,
  Star,
  Landmark,
  Check,
  Quote,
} from "lucide-react";
import { ThemeToggle } from "@/components/ThemeToggle";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "FinTrack — Controla tus gastos y evita sustos con el banco" },
      {
        name: "description",
        content:
          "FinTrack te dice cuánto puedes gastar cada mes y avisa al instante si detecta un cargo raro en tus cuentas. Sin hojas de cálculo.",
      },
      { property: "og:title", content: "FinTrack — Tu dinero, sin sorpresas" },
      {
        property: "og:description",
        content:
          "Presupuestos claros por categoría y un escudo que frena los cargos sospechosos antes de que te quiten el dinero.",
      },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: Landing,
});

const metrics = [
  { value: "Al instante", label: "Alertas de cargos anómalos" },
  { value: "6", label: "Categorías automáticas" },
  { value: "0 €", label: "Coste de inicio" },
  { value: "2 min", label: "Conexión bancaria" },
];

const features = [
  {
    icon: Wallet,
    title: "Presupuestos claros",
    text: "Define límites mensuales por categoría y consulta en cualquier momento cuánto has gastado y cuánto te queda disponible.",
    accent: "text-primary",
    items: ["Límites personalizables", "Alertas antes de superarlos", "Reinicio mensual automático"],
  },
  {
    icon: ShieldCheck,
    title: "Protección antifraude",
    text: "Detecta pagos que no encajan con tu perfil de uso y los detiene para que tú valides si son legítimos antes de que se procesen.",
    accent: "text-fraud",
    items: ["Bloqueo preventivo", "Validación por el usuario", "Aprendizaje continuo"],
  },
  {
    icon: FileUp,
    title: "Sincronización automática",
    text: "Conecta tu cuenta bancaria de forma segura o importa tu extracto en CSV. Nosotros clasificamos y organizan todos los movimientos.",
    accent: "text-income",
    items: ["Conexión bancaria segura", "Importación por arrastre", "Sin movimientos duplicados"],
  },
];

const budgets = [
  { name: "Alimentación", spent: "382,40", limit: "500,00", pct: 76, state: "Vas bien", tone: "income" },
  { name: "Vivienda", spent: "846,00", limit: "900,00", pct: 94, state: "Casi al tope", tone: "warning" },
  { name: "Ocio", spent: "200,00", limit: "200,00", pct: 100, state: "Te has pasado", tone: "fraud" },
  { name: "Transporte", spent: "54,90", limit: "150,00", pct: 37, state: "Vas bien", tone: "income" },
];

const toneClasses: Record<string, { bar: string; text: string; chip: string }> = {
  income: { bar: "bg-income", text: "text-income", chip: "bg-income-soft text-income" },
  warning: { bar: "bg-warning", text: "text-warning", chip: "bg-warning-soft text-warning" },
  fraud: { bar: "bg-fraud", text: "text-fraud", chip: "bg-fraud-soft text-fraud" },
};

const banks = ["BBVA", "Santander", "CaixaBank", "Revolut", "N26", "Sabadell"];

const certs = [
  { label: "Datos cifrados", note: "Nivel bancario" },
  { label: "Sin claves", note: "Acceso consciente" },
  { label: "Control total", note: "Revoca cuando quieras" },
];

const steps = [
  {
    n: "01",
    title: "Conecta tu cuenta",
    text: "Autoriza el acceso desde tu banco o importa tu extracto. En un par de minutos tendrás toda la información organizada.",
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



function Landing() {
  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-20 border-b border-border bg-background/85 backdrop-blur">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-5">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-sm font-bold text-primary-foreground">
              FT
            </div>
            <span className="text-base font-bold tracking-tight">FinTrack</span>
            <span className="hidden rounded-full border border-border bg-surface px-2.5 py-0.5 text-[11px] text-muted-foreground sm:inline">
              Gestión + seguridad
            </span>
          </div>
          <div className="flex items-center gap-2">
            <ThemeToggle />
            <button className="hidden h-9 items-center rounded-md px-3 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground sm:inline-flex">
              Iniciar Sesión
            </button>
            <button className="inline-flex h-9 items-center rounded-md bg-primary px-4 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90">
              Crear Cuenta
            </button>
          </div>
        </div>
      </header>

      <main>
        {/* HERO */}
        <section className="mx-auto max-w-6xl px-5 pt-16 pb-14 sm:pt-24">
          <div className="inline-flex items-center gap-2 rounded-full border border-border bg-surface px-3 py-1 text-xs text-muted-foreground">
            <Zap size={13} className="text-warning" />
            Control de gastos y alertas de seguridad en tiempo real
          </div>
          <h1 className="mt-6 max-w-3xl text-4xl font-extrabold leading-[1.08] tracking-tight sm:text-5xl md:text-[3.4rem]">
            Tu dinero bajo control, sin sorpresas a final de mes
          </h1>
          <p className="mt-5 max-w-2xl text-base leading-relaxed text-muted-foreground">
            FinTrack te muestra cuánto puedes gastar en cada categoría y te alerta al instante si
            detecta un cargo anómalo, para que puedas revisarlo antes de que se procese.
          </p>
          <div className="mt-8 flex flex-wrap items-center gap-3">
            <button className="inline-flex h-11 items-center gap-2 rounded-md bg-primary px-5 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90">
              Empezar gratis <ArrowRight size={16} />
            </button>
            <button className="inline-flex h-11 items-center rounded-md border border-border bg-surface px-5 text-sm font-semibold transition-colors hover:bg-accent">
              Ver cómo funciona
            </button>
          </div>

          <dl className="mt-14 grid grid-cols-2 divide-border overflow-hidden rounded-xl border border-border bg-surface sm:grid-cols-4 sm:divide-x">
            {metrics.map((m) => (
              <div key={m.label} className="border-t border-border px-5 py-5 first:border-t-0 sm:border-t-0">
                <dd className="num text-2xl font-semibold">{m.value}</dd>
                <dt className="mt-1 text-xs text-muted-foreground">{m.label}</dt>
              </div>
            ))}
          </dl>
        </section>

        {/* TRUST */}
        <section className="border-t border-border bg-surface">
          <div className="mx-auto max-w-6xl px-5 py-10">
            <div className="flex flex-col gap-8 lg:flex-row lg:items-start lg:justify-between">
              <div>
                <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">
                  Funciona con tu banco
                </p>
                <div className="mt-4 flex flex-wrap items-center gap-x-6 gap-y-3">
                  {banks.map((b) => (
                    <span
                      key={b}
                      className="flex items-center gap-2 text-sm font-semibold text-muted-foreground"
                    >
                      <Landmark size={14} className="text-primary" />
                      {b}
                    </span>
                  ))}
                </div>
              </div>
              <div className="flex flex-wrap gap-3">
                {certs.map((c) => (
                  <div
                    key={c.label}
                    className="flex items-center gap-2 rounded-lg border border-border bg-card px-3 py-2"
                  >
                    <BadgeCheck size={15} className="text-income" />
                    <div>
                      <p className="text-xs font-semibold">{c.label}</p>
                      <p className="text-[11px] text-muted-foreground">{c.note}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <figure className="mt-8 border-t border-border pt-8 lg:flex lg:items-center lg:gap-8">
                <blockquote className="flex-1">
                <Quote size={16} className="text-primary" />
                <p className="mt-3 max-w-2xl text-sm leading-relaxed text-foreground">
                  «Nos ayudó a detectar un cargo duplicado de un proveedor antes incluso de revisar el
                  extracto. Desde entonces, el presupuesto familiar ya no se descuadra a final de mes.»
                </p>
                <figcaption className="mt-3 text-xs text-muted-foreground">
                  Marta Ferrán · usuaria desde 2025
                </figcaption>
              </blockquote>
              <div className="mt-6 flex items-center gap-3 lg:mt-0">
                <div className="flex gap-0.5">
                  {[0, 1, 2, 3, 4].map((i) => (
                    <Star key={i} size={14} className="fill-warning text-warning" />
                  ))}
                </div>
                <p className="num text-sm font-semibold">
                  4,8<span className="text-muted-foreground">/5 · 1.240 valoraciones</span>
                </p>
              </div>
            </figure>
          </div>
        </section>


        {/* FEATURES */}
        <section className="border-t border-border">
          <div className="mx-auto max-w-6xl px-5 py-16">
            <h2 className="text-2xl font-bold tracking-tight sm:text-3xl">
              Control y seguridad en un solo lugar
            </h2>
            <p className="mt-3 max-w-xl text-sm text-muted-foreground">
              Todo lo que necesitas para gestionar tu dinero y protegerte de cargos no autorizados.
            </p>
            <div className="mt-10 grid gap-5 md:grid-cols-3">
              {features.map((f) => (
                <article
                  key={f.title}
                  className="rounded-xl border border-border bg-card p-6 transition-colors hover:border-primary/40"
                >
                  <f.icon size={20} className={f.accent} />
                  <h3 className="mt-4 text-base font-semibold">{f.title}</h3>
                  <p className="mt-2 text-sm leading-relaxed text-muted-foreground">{f.text}</p>
                  <ul className="mt-4 space-y-2 border-t border-border pt-4">
                    {f.items.map((i) => (
                      <li key={i} className="flex items-center gap-2 text-xs text-muted-foreground">
                        <span className="h-1 w-1 rounded-full bg-muted-foreground" />
                        {i}
                      </li>
                    ))}
                  </ul>
                </article>
              ))}
            </div>
          </div>
        </section>

        {/* HOW IT WORKS */}
        <section className="border-t border-border bg-surface">
          <div className="mx-auto max-w-6xl px-5 py-16">
            <h2 className="text-2xl font-bold tracking-tight sm:text-3xl">Cómo funciona</h2>
            <p className="mt-3 max-w-xl text-sm text-muted-foreground">
              Tres pasos claros, sin complicaciones.
            </p>
            <ol className="mt-10 grid gap-px overflow-hidden rounded-xl border border-border bg-border md:grid-cols-3">
              {steps.map((s) => (
                <li key={s.n} className="bg-card p-6">
                  <span className="num text-xs font-semibold text-primary">{s.n}</span>
                  <h3 className="mt-3 text-base font-semibold">{s.title}</h3>
                  <p className="mt-2 text-sm leading-relaxed text-muted-foreground">{s.text}</p>
                </li>
              ))}
            </ol>
          </div>
        </section>


        {/* DASHBOARD PREVIEW */}
        <section className="border-t border-border">
          <div className="mx-auto max-w-6xl px-5 py-16">
            <h2 className="text-2xl font-bold tracking-tight sm:text-3xl">Tu panel de control</h2>
            <p className="mt-3 max-w-xl text-sm text-muted-foreground">
              Alertas prioritarias al inicio y tus presupuestos organizados. Todo visible de una ojeada.
            </p>

            <div className="mt-8 overflow-hidden rounded-xl border border-border bg-card">
              <div className="flex items-center justify-between border-b border-border px-5 py-3">
                <div className="flex items-center gap-2 text-sm font-semibold">
                  <Lock size={14} className="text-primary" /> Resumen del mes
                </div>
                <span className="num text-xs text-muted-foreground">Julio 2026</span>
              </div>

              <div className="flex flex-wrap items-center gap-3 border-b border-border bg-warning-soft px-5 py-3">
                <TriangleAlert size={15} className="text-warning" />
                <p className="text-sm font-medium text-warning">
                  Movimiento bloqueado a la espera de tu confirmación
                </p>
                <span className="num ml-auto text-xs text-warning">−128,90 €</span>
              </div>
              <div className="border-b border-border px-5 py-4">
                <div className="flex items-baseline justify-between">
                  <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                    Motivos de la alerta
                  </p>
                  <p className="text-xs text-fraud">Pendiente de validación</p>
                </div>
                <ul className="mt-3 grid gap-2 sm:grid-cols-2">
                  {rules.map((r) => {
                    const t = toneClasses[r.tone];
                    return (
                      <li
                        key={r.label}
                        className="flex items-center justify-between gap-3 rounded-md border border-border px-3 py-2"
                      >
                        <span className="text-xs text-muted-foreground">{r.label}</span>
                        <span className={`text-xs font-semibold ${t.text}`}>{r.score}</span>
                      </li>
                    );
                  })}
                </ul>
              </div>



              <div className="grid gap-px bg-border sm:grid-cols-2">
                {budgets.map((b) => {
                  const t = toneClasses[b.tone];
                  return (
                    <div key={b.name} className="bg-card p-5">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-semibold">{b.name}</span>
                        <span className={`rounded-full px-2 py-0.5 text-[11px] font-medium ${t.chip}`}>
                          {b.state}
                        </span>
                      </div>
                      <div className="mt-3 flex items-baseline justify-between">
                        <span className="num text-lg font-semibold">{b.spent} €</span>
                        <span className="num text-xs text-muted-foreground">de {b.limit} €</span>
                      </div>
                      <div className="mt-3 h-1.5 w-full overflow-hidden rounded-full bg-muted">
                        <div className={`h-full rounded-full ${t.bar}`} style={{ width: `${b.pct}%` }} />
                      </div>
                      <div className={`num mt-2 text-xs ${t.text}`}>{b.pct}%</div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </section>

        {/* TECH DETAILS (para perfiles técnicos) */}
        <section className="border-t border-border bg-surface">
          <div className="mx-auto max-w-6xl px-5 py-16">
            <div className="lg:flex lg:items-end lg:justify-between lg:gap-10">
              <div>
                <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">
                  Especificaciones técnicas
                </p>
                <h2 className="mt-3 text-2xl font-bold tracking-tight sm:text-3xl">
                  Tecnología de confianza
                </h2>
                <p className="mt-3 max-w-xl text-sm text-muted-foreground">
                  Detalles técnicos para quienes quieran conocerlos. El sistema funciona de forma
                  automática para el resto de usuarios.
                </p>
              </div>
            </div>
            <dl className="mt-8 grid gap-px overflow-hidden rounded-xl border border-border bg-border sm:grid-cols-2 lg:grid-cols-3">
              {techSpecs.map((s) => (
                <div key={s.label} className="bg-card px-5 py-4">
                  <dt className="text-xs text-muted-foreground">{s.label}</dt>
                  <dd className="num mt-1 text-sm font-semibold">{s.value}</dd>
                </div>
              ))}
            </dl>
          </div>
        </section>

        {/* FAQ */}
        <section className="border-t border-border bg-surface">
          <div className="mx-auto grid max-w-6xl gap-10 px-5 py-16 lg:grid-cols-[0.8fr_1.2fr]">
            <div>
              <h2 className="text-2xl font-bold tracking-tight sm:text-3xl">Preguntas frecuentes</h2>
              <p className="mt-3 max-w-sm text-sm text-muted-foreground">
                Respuestas a las dudas más comunes antes de conectar una cuenta bancaria.
              </p>
            </div>
            <dl className="divide-y divide-border border-y border-border">
              {faqs.map((f) => (
                <div key={f.q} className="py-5">
                  <dt className="text-sm font-semibold">{f.q}</dt>
                  <dd className="mt-2 text-sm leading-relaxed text-muted-foreground">{f.a}</dd>
                </div>
              ))}
            </dl>
          </div>
        </section>

        {/* FINAL CTA */}
        <section className="border-t border-border">
          <div className="mx-auto max-w-6xl px-5 py-16">
            <div className="rounded-xl border border-border bg-card p-8 sm:p-10">
              <h2 className="max-w-xl text-2xl font-bold tracking-tight sm:text-3xl">
                Empieza hoy con mejor tranquilidad financiera
              </h2>
              <p className="mt-3 max-w-xl text-sm leading-relaxed text-muted-foreground">
                Controla cada euro y protege tus cuentas de cargos no autorizados. Sin coste para
                comenzar.
              </p>
              <div className="mt-7 flex flex-wrap items-center gap-3">
                <button className="inline-flex h-11 items-center gap-2 rounded-md bg-primary px-5 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90">
                  Crear mi cuenta <ArrowRight size={16} />
                </button>
                <button className="inline-flex h-11 items-center rounded-md border border-border bg-surface px-5 text-sm font-semibold transition-colors hover:bg-accent">
                  Hablar con nosotros
                </button>
              </div>
              <ul className="mt-6 flex flex-wrap gap-x-6 gap-y-2">
                {[
                  "Sin tarjeta de crédito",
                  "Exporta tus datos cuando quieras",
                  "Revoca el acceso en cualquier momento",
                ].map((i) => (
                    <li key={i} className="flex items-center gap-2 text-xs text-muted-foreground">
                      <Check size={13} className="text-income" />
                      {i}
                    </li>
                  ))}
              </ul>
            </div>
          </div>
        </section>
      </main>

      <footer className="border-t border-border bg-surface">
        <div className="mx-auto max-w-6xl px-5 py-12">
          <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
            <div>
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-xs font-bold text-primary-foreground">
                  FT
                </div>
                <span className="text-sm font-bold tracking-tight">FinTrack &amp; FraudShield</span>
              </div>
              <p className="mt-3 max-w-xs text-xs leading-relaxed text-muted-foreground">
                Control de gastos y protección antifraude para tu día a día.
              </p>
            </div>
            {[
              { title: "Producto", links: ["Acceso Usuarios", "Panel", "Subir extracto", "Conectar banco"] },
              { title: "Legal", links: ["Aviso legal", "Privacidad", "Cookies", "Términos"] },
              { title: "Soporte", links: ["Centro de ayuda", "Contacto", "Estado del servicio", "Seguridad"] },
            ].map((col) => (
              <nav key={col.title}>
                <p className="text-xs font-semibold uppercase tracking-wider text-foreground">
                  {col.title}
                </p>
                <ul className="mt-3 space-y-2">
                  {col.links.map((l) => (
                    <li key={l}>
                      <a
                        href="#"
                        className="text-xs text-muted-foreground transition-colors hover:text-foreground"
                      >
                        {l}
                      </a>
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
