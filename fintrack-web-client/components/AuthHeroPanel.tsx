"use client";

import { ShieldCheck, Zap, Lock, CheckCircle2 } from "lucide-react";

/**
 * Propiedades para el componente AuthHeroPanel.
 *
 * @property mode modo de presentación del panel ('login' o 'register')
 */
interface AuthHeroPanelProps {
  mode: "login" | "register";
}

/**
 * Componente visual de presentación lateral para páginas de autenticación.
 * <p>
 * Muestra el branding de FinTrack, el badge del motor de seguridad FraudShield
 * y métricas destacadas siguiendo la guía de diseño visual (.agents/rules/ui_design_guide.md).
 * </p>
 *
 * @param props propiedades del componente {@link AuthHeroPanelProps}
 * @returns elemento JSX del panel lateral informativo
 */
export default function AuthHeroPanel({ mode }: AuthHeroPanelProps) {
  const isLogin = mode === "login";

  return (
    <div className="relative flex h-full flex-col justify-between overflow-hidden bg-slate-900 text-slate-100 p-8 lg:p-12 border-r border-slate-800">
      {/* Subtle Background Glow conforming to UI rules */}
      <div className="pointer-events-none absolute -top-24 -left-24 h-96 w-96 rounded-full bg-blue-600/10 blur-3xl" />
      <div className="pointer-events-none absolute -bottom-24 -right-24 h-96 w-96 rounded-full bg-emerald-600/10 blur-3xl" />

      {/* Top Header Branding */}
      <div className="relative z-10 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-600 font-bold text-white shadow-xs">
            FT
          </div>
          <div>
            <h1 className="text-lg font-semibold tracking-tight text-white">
              FinTrack
            </h1>
            <p className="text-xs text-slate-400 font-normal">
              Ecosistema Financiero &amp; FraudShield
            </p>
          </div>
        </div>

        {/* Live Badge */}
        <div className="inline-flex items-center gap-2 rounded-full border border-slate-700 bg-slate-800/80 px-3 py-1 text-xs text-slate-300">
          <span className="relative flex h-2 w-2">
            <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75" />
            <span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-500" />
          </span>
          <span className="num font-semibold text-emerald-400">Protección Activa</span>
        </div>
      </div>

      {/* Center Visual Hero Area */}
      <div className="relative z-10 my-auto py-8">
        <div className="inline-flex items-center gap-2 rounded-lg border border-blue-500/30 bg-blue-500/10 px-3 py-1 text-xs font-semibold text-blue-400 mb-4">
          <Zap size={14} className="text-blue-400" />
          <span>{isLogin ? "Monitoreo Financiero Activo" : "Registro Seguro de Usuario"}</span>
        </div>

        <h2 className="text-2xl font-semibold leading-snug text-white sm:text-3xl lg:text-4xl">
          {isLogin
            ? "Accede a tu panel de control y seguridad financiera"
            : "Toma el control total de tus presupuestos personales"}
        </h2>

        <p className="mt-3 max-w-lg text-sm text-slate-300 leading-relaxed font-normal">
          {isLogin
            ? "Visualiza el estado de tus cuentas, presupuestos categorizados y alertas de detección de fraude en tiempo real."
            : "Crea tu cuenta en minutos. Sin permanencia ni costes ocultos, respaldado por arquitectura de microservicios."}
        </p>

        {/* FraudShield Mock Card Display (Clean Fintech Aesthetic) */}
        <div className="mt-8 rounded-xl border border-slate-800 bg-slate-950/70 p-5 shadow-xs backdrop-blur-xs">
          <div className="flex items-center justify-between border-b border-slate-800 pb-3">
            <div className="flex items-center gap-2.5">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                <ShieldCheck size={18} />
              </div>
              <div>
                <p className="text-xs font-semibold text-white">FraudShield Engine v2.4</p>
                <p className="text-[11px] text-slate-400">Verificación en streaming activo</p>
              </div>
            </div>
            <span className="rounded-md border border-emerald-500/30 bg-emerald-500/10 px-2 py-0.5 text-[11px] font-semibold text-emerald-400">
              OK
            </span>
          </div>

          <div className="mt-4 grid grid-cols-3 gap-3 text-left">
            <div className="rounded-lg border border-slate-800/80 bg-slate-900/60 p-3">
              <p className="text-[11px] text-slate-400 font-normal">Transacciones</p>
              <p className="num mt-1 text-base font-semibold text-white">+120.450</p>
            </div>
            <div className="rounded-lg border border-slate-800/80 bg-slate-900/60 p-3">
              <p className="text-[11px] text-slate-400 font-normal">Precisión Engine</p>
              <p className="num mt-1 text-base font-semibold text-emerald-400">99,9%</p>
            </div>
            <div className="rounded-lg border border-slate-800/80 bg-slate-900/60 p-3">
              <p className="text-[11px] text-slate-400 font-normal">Cifrado</p>
              <p className="num mt-1 text-base font-semibold text-blue-400">AES-256</p>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Feature Items */}
      <div className="relative z-10 border-t border-slate-800 pt-6">
        <ul className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <li className="flex items-center gap-2 text-xs text-slate-300 font-normal">
            <CheckCircle2 size={15} className="text-emerald-400 shrink-0" />
            <span>PSD2 / SCA Seguro</span>
          </li>
          <li className="flex items-center gap-2 text-xs text-slate-300 font-normal">
            <CheckCircle2 size={15} className="text-emerald-400 shrink-0" />
            <span>Categorización Automática</span>
          </li>
          <li className="flex items-center gap-2 text-xs text-slate-300 font-normal">
            <CheckCircle2 size={15} className="text-emerald-400 shrink-0" />
            <span>Sin datos compartidos</span>
          </li>
        </ul>
      </div>
    </div>
  );
}
