import React, { useState } from "react";
import MobileBottomSheet from "@/components/MobileBottomSheet";
import { Transaction } from "@/types/dashboard";
import { ToastType } from "@/components/ToastNotification";
import { finTrackApi } from "@/lib/api";

interface AddExpenseModalProps {
  isOpen: boolean;
  onClose: () => void;
  setTransactions: React.Dispatch<React.SetStateAction<Transaction[]>>;
  setAccountBalance: React.Dispatch<React.SetStateAction<number>>;
  setToast: (toast: { isOpen: boolean; message: string; type: ToastType }) => void;
}

/**
 * Modal Bottom Sheet para ingresar un nuevo gasto manual.
 */
export default function AddExpenseModal({
  isOpen,
  onClose,
  setTransactions,
  setAccountBalance,
  setToast,
}: AddExpenseModalProps) {
  const [newMerchant, setNewMerchant] = useState<string>("");
  const [newAmount, setNewAmount] = useState<string>("");

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
    setNewMerchant("");
    setNewAmount("");
    onClose();

    setToast({
      isOpen: true,
      message: isBackendActive
        ? `Gasto de ${parsedAmount.toFixed(2)} € enviado a clasificación en Kafka`
        : `Gasto de ${parsedAmount.toFixed(2)} € registrado (modo local)`,
      type: "success",
    });

    if (isBackendActive) {
      setTimeout(async () => {
        try {
          const [summaryRes, txsRes] = await Promise.allSettled([
            finTrackApi.getAccountSummary(),
            finTrackApi.getTransactions(),
          ]);
          if (summaryRes.status === "fulfilled" && summaryRes.value) {
            setAccountBalance(summaryRes.value.balance);
          }
          if (txsRes.status === "fulfilled" && txsRes.value && txsRes.value.length > 0) {
            const updatedTxs: Transaction[] = txsRes.value.map((t) => ({
              id: t.id,
              merchant: t.merchant,
              date: t.timestamp ? new Date(t.timestamp).toLocaleDateString("es-ES") : "Hoy",
              amount: t.amount,
              category: t.category,
              status: t.status === "QUARANTINED" ? "SUSPICIOUS" : t.status === "PROCESSED" ? "CLEAN" : "PENDING",
            }));
            setTransactions(updatedTxs);
          }
        } catch {}
      }, 1800);
    }
  };

  return (
    <MobileBottomSheet
      isOpen={isOpen}
      onClose={onClose}
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
  );
}
