"use client";

import React, { useEffect } from "react";
import { AlertTriangle, Trash2, HelpCircle, X } from "lucide-react";

/**
 * Variantes visuales para el modal de confirmación {@link ConfirmDialog}.
 */
export type ConfirmDialogVariant = "danger" | "warning" | "info";

/**
 * Propiedades del componente {@link ConfirmDialog}.
 */
export interface ConfirmDialogProps {
  /**
   * Indica si el diálogo de confirmación está abierto.
   */
  isOpen: boolean;

  /**
   * Título principal del modal de confirmación.
   */
  title: string;

  /**
   * Mensaje o cuerpo explicativo mostrado al usuario.
   */
  message: string;

  /**
   * Texto para el botón de confirmación. Valor por defecto: `"Confirmar"`.
   */
  confirmText?: string;

  /**
   * Texto para el botón de cancelación. Valor por defecto: `"Cancelar"`.
   */
  cancelText?: string;

  /**
   * Variante visual que determina los colores semánticos e icono principal.
   * Valor por defecto: `"danger"`.
   */
  variant?: ConfirmDialogVariant;

  /**
   * Callback invocado al hacer clic en el botón de confirmación.
   */
  onConfirm: () => void;

  /**
   * Callback invocado al hacer clic en cancelar o presionar la tecla Escape.
   */
  onCancel: () => void;
}

/**
 * Componente cliente modal reutilizable para la confirmación de acciones destructivas o sensibles (**ConfirmDialog**).
 * <p>
 * Previene acciones accidentales (como la eliminación de presupuestos o transacciones) pidiendo
 * confirmación explícita al usuario. Cumple con la guía de estilo de FinTrack, accesibilidad WCAG AA
 * y atajos de teclado (`Escape`).
 * </p>
 *
 * @param props - Propiedades descritas en {@link ConfirmDialogProps}
 * @returns Elemento JSX del modal o `null` si `isOpen` es falso
 */
export default function ConfirmDialog({
  isOpen,
  title,
  message,
  confirmText = "Confirmar",
  cancelText = "Cancelar",
  variant = "danger",
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  // Prevenir desplazamiento del body y escuchar tecla Escape
  useEffect(() => {
    if (!isOpen) return;

    document.body.style.overflow = "hidden";
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        onCancel();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => {
      document.body.style.overflow = "";
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [isOpen, onCancel]);

  if (!isOpen) return null;

  // Iconos y colores según la variante semántica
  const renderIcon = () => {
    switch (variant) {
      case "danger":
        return (
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-red-500/10 text-red-600 dark:text-red-400 border border-red-500/20">
            <Trash2 className="h-5 w-5" />
          </div>
        );
      case "warning":
        return (
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-amber-500/10 text-amber-600 dark:text-amber-400 border border-amber-500/20">
            <AlertTriangle className="h-5 w-5" />
          </div>
        );
      default:
        return (
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary border border-primary/20">
            <HelpCircle className="h-5 w-5" />
          </div>
        );
    }
  };

  const getConfirmButtonClasses = () => {
    switch (variant) {
      case "danger":
        return "bg-red-600 hover:bg-red-700 text-white";
      case "warning":
        return "bg-amber-600 hover:bg-amber-700 text-white";
      default:
        return "bg-primary hover:opacity-90 text-primary-foreground";
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 backdrop-blur-xs p-4 animate-in fade-in duration-200"
      onClick={onCancel}
      role="alertdialog"
      aria-modal="true"
      aria-labelledby="confirm-dialog-title"
      aria-describedby="confirm-dialog-message"
    >
      <div
        className="w-full max-w-md rounded-2xl bg-card border border-border p-6 shadow-2xl animate-in zoom-in-95 duration-200"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between gap-3 mb-4">
          <div className="flex items-start gap-3">
            {renderIcon()}
            <div>
              <h3
                id="confirm-dialog-title"
                className="text-base font-bold text-foreground tracking-tight"
              >
                {title}
              </h3>
              <p
                id="confirm-dialog-message"
                className="mt-1 text-xs text-muted-foreground font-medium leading-relaxed"
              >
                {message}
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onCancel}
            className="rounded-lg p-1 text-muted-foreground hover:bg-accent hover:text-foreground transition cursor-pointer"
            title="Cerrar"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        {/* Acciones del diálogo */}
        <div className="mt-6 flex items-center justify-end gap-2.5 pt-3 border-t border-border">
          <button
            type="button"
            onClick={onCancel}
            className="rounded-xl border border-border bg-card px-4 py-2 text-xs font-bold text-foreground hover:bg-accent active:scale-95 transition cursor-pointer"
          >
            {cancelText}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className={`rounded-xl px-4 py-2 text-xs font-bold transition shadow-xs active:scale-95 cursor-pointer ${getConfirmButtonClasses()}`}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}
