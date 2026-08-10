"use client";

import React, { useEffect } from "react";
import { CheckCircle2, AlertTriangle, Info, X } from "lucide-react";

/**
 * Tipos de notificaciones soportadas por {@link ToastNotification}.
 */
export type ToastType = "success" | "error" | "info";

/**
 * Propiedades de configuración del componente {@link ToastNotification}.
 */
export interface ToastNotificationProps {
  /**
   * Indica si la notificación Toast está activa y visible.
   */
  isOpen: boolean;

  /**
   * Mensaje descriptivo a mostrar en la notificación.
   */
  message: string;

  /**
   * Tipo semántico de la notificación (`success`, `error`, `info`). Valor por defecto: `"success"`.
   */
  type?: ToastType;

  /**
   * Duración en milisegundos antes de que el Toast se auto-descarte automáticamente.
   * Valor por defecto: `5000` (5 segundos).
   */
  durationMs?: number;

  /**
   * Función callback invocada cuando el temporizador expira o el usuario cierra el Toast manualmente.
   */
  onClose: () => void;
}

/**
 * Componente cliente flotante reutilizable para notificaciones tipo Toast (**ToastNotification**).
 * <p>
 * Muestra alertas efímeras en la esquina inferior derecha con un temporizador automático que desvanece
 * la notificación a los 5 segundos (`5000ms`). Cumple con la guía de estilo de FinTrack y accesibilidad.
 * </p>
 *
 * @param props - Propiedades descritas en {@link ToastNotificationProps}
 * @returns Elemento JSX con el Toast o `null` si `isOpen` es falso
 */
export default function ToastNotification({
  isOpen,
  message,
  type = "success",
  durationMs = 5000,
  onClose,
}: ToastNotificationProps) {
  // Temporizador de auto-descarte a los 5 segundos (5000ms)
  useEffect(() => {
    if (!isOpen) return;

    const timer = setTimeout(() => {
      onClose();
    }, durationMs);

    return () => {
      clearTimeout(timer);
    };
  }, [isOpen, durationMs, onClose]);

  if (!isOpen) return null;

  // Iconos y estilos semánticos
  const renderIcon = () => {
    switch (type) {
      case "success":
        return <CheckCircle2 className="h-4 w-4 text-emerald-600 dark:text-emerald-400 shrink-0" />;
      case "error":
        return <AlertTriangle className="h-4 w-4 text-red-600 dark:text-red-400 shrink-0" />;
      default:
        return <Info className="h-4 w-4 text-primary shrink-0" />;
    }
  };

  const getBorderColorClass = () => {
    switch (type) {
      case "success":
        return "border-emerald-500/30";
      case "error":
        return "border-red-500/30";
      default:
        return "border-border";
    }
  };

  return (
    <div
      className={`fixed bottom-16 sm:bottom-6 right-4 z-50 flex items-center justify-between gap-3 rounded-2xl border ${getBorderColorClass()} bg-card/95 backdrop-blur-md px-4 py-3 shadow-lg max-w-sm animate-in slide-in-from-bottom-2 duration-300`}
      role="status"
      aria-live="polite"
    >
      <div className="flex items-center gap-2.5">
        {renderIcon()}
        <span className="text-xs font-bold text-foreground tracking-tight">
          {message}
        </span>
      </div>

      <button
        type="button"
        onClick={onClose}
        className="rounded-lg p-1 text-muted-foreground hover:bg-accent hover:text-foreground transition cursor-pointer"
        title="Cerrar aviso"
      >
        <X className="h-3.5 w-3.5" />
      </button>
    </div>
  );
}
