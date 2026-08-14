import React from "react";
import MobileBottomSheet from "@/components/MobileBottomSheet";
import { Transaction } from "@/types/dashboard";

interface ReviewFraudModalProps {
  isOpen: boolean;
  onClose: () => void;
  suspiciousTx?: Transaction;
  setTransactions: React.Dispatch<React.SetStateAction<Transaction[]>>;
  setAlertDismissed: React.Dispatch<React.SetStateAction<boolean>>;
}

/**
 * Modal Bottom Sheet para revisar y gestionar transacciones en cuarentena por FraudShield.
 */
export default function ReviewFraudModal({
  isOpen,
  onClose,
  suspiciousTx,
  setTransactions,
  setAlertDismissed,
}: ReviewFraudModalProps) {
  const targetId = suspiciousTx?.id || "tx-102";
  const amount = suspiciousTx ? Math.abs(suspiciousTx.amount).toFixed(2) : "850,00";
  const merchant = suspiciousTx?.merchant || "Transferencia Desconocida (Overseas)";

  const handleAuthorize = () => {
    setTransactions((prev) =>
      prev.map((t) => (t.id === targetId ? { ...t, status: "CLEAN" } : t))
    );
    setAlertDismissed(true);
    onClose();
  };

  const handleBlock = () => {
    setTransactions((prev) => prev.filter((t) => t.id !== targetId));
    setAlertDismissed(true);
    onClose();
  };

  return (
    <MobileBottomSheet
      isOpen={isOpen}
      onClose={onClose}
      title="Detalle de Transacción en Cuarentena"
    >
      <div className="space-y-4">
        <div className="rounded-xl bg-amber-500/10 border border-amber-500/20 p-4 text-amber-900 dark:text-amber-200">
          <div className="text-xs font-bold uppercase tracking-wider text-amber-700 dark:text-amber-300 mb-1">
            Retención de Seguridad
          </div>
          <div className="text-lg font-bold num">{amount} €</div>
          <div className="text-xs font-medium mt-1">{merchant}</div>
          <div className="text-[11px] text-muted-foreground mt-0.5">Hoy • Origen no habitual detectado</div>
        </div>

        <p className="text-xs text-muted-foreground font-medium">
          El sistema de protección FraudShield ha retenido esta operación por detectarla fuera de tu radio geográfico y hábito de consumo habitual.
        </p>

        <div className="grid grid-cols-2 gap-2 pt-2">
          <button
            type="button"
            onClick={handleAuthorize}
            aria-label="Confirmar transacción como legítima"
            className="rounded-xl bg-emerald-600 py-2.5 text-xs font-bold text-white hover:bg-emerald-700 active:scale-95 transition shadow-xs cursor-pointer text-center"
          >
            Autorizar Transacción
          </button>
          <button
            type="button"
            onClick={handleBlock}
            aria-label="Confirmar transacción como fraude y bloquear"
            className="rounded-xl bg-red-600 py-2.5 text-xs font-bold text-white hover:bg-red-700 active:scale-95 transition shadow-xs cursor-pointer text-center"
          >
            Confirmar Fraude
          </button>
        </div>
      </div>
    </MobileBottomSheet>
  );
}
