import React from "react";
import { AlertTriangle } from "lucide-react";
import { Transaction } from "@/types/dashboard";

interface FraudAlertBannerProps {
  suspiciousTx: Transaction;
  onReview: () => void;
  onDismiss: () => void;
}

/**
 * Banner contextual para avisos de transacciones en cuarentena por FraudShield.
 */
export default function FraudAlertBanner({ suspiciousTx, onReview, onDismiss }: FraudAlertBannerProps) {
  return (
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
          onClick={onReview}
          className="rounded-lg bg-amber-600 px-3.5 py-1.5 text-xs font-bold text-white hover:bg-amber-700 active:scale-95 transition shadow-xs cursor-pointer"
        >
          Revisar Cuarentena
        </button>
        <button
          type="button"
          onClick={onDismiss}
          className="rounded-lg p-1.5 text-amber-700 hover:bg-amber-500/20 dark:text-amber-400 transition cursor-pointer"
          title="Descartar aviso"
        >
          ✕
        </button>
      </div>
    </div>
  );
}
