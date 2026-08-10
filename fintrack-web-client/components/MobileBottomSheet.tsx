"use client";

import React, { useEffect } from "react";
import { X } from "lucide-react";

/**
 * Propiedades del componente {@link MobileBottomSheet}.
 */
export interface MobileBottomSheetProps {
  /**
   * Indica si la hoja emergente inferior se encuentra visible.
   */
  isOpen: boolean;

  /**
   * Función callback invocada para cerrar la hoja emergente.
   */
  onClose: () => void;

  /**
   * Título principal mostrado en el encabezado del Bottom Sheet.
   */
  title: string;

  /**
   * Contenido JSX o componentes secundarios renderizados dentro del cuerpo del Bottom Sheet.
   */
  children: React.ReactNode;
}

/**
 * Componente cliente interactivo de hoja emergente inferior (**Mobile Bottom Sheet**).
 * <p>
 * Se desliza suavemente desde la parte inferior de la pantalla en dispositivos móviles
 * y se muestra como un modal flotante centrado en pantallas más grandes.
 * Cumple con los estándares de accesibilidad WCAG AA y la guía de estilos de FinTrack.
 * </p>
 *
 * @param props - Propiedades de configuración descritas en {@link MobileBottomSheetProps}
 * @returns Elemento JSX con el modal inferior o `null` si no está activo
 */
export default function MobileBottomSheet({
  isOpen,
  onClose,
  title,
  children,
}: MobileBottomSheetProps) {
  // Prevenir el desplazamiento del cuerpo (scroll locking) mientras la hoja está abierta
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }
    return () => {
      document.body.style.overflow = "";
    };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-slate-950/60 backdrop-blur-xs transition-opacity animate-in fade-in duration-200"
      onClick={onClose}
      role="dialog"
      aria-modal="true"
      aria-labelledby="bottom-sheet-title"
    >
      <div
        className="w-full max-w-lg rounded-t-2xl sm:rounded-2xl bg-card border-t sm:border border-border p-6 shadow-2xl transition-all animate-in slide-in-from-bottom duration-300 max-h-[85vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Tirador táctil para dispositivos móviles (Drag handle visual) */}
        <div className="mx-auto mb-4 h-1.5 w-12 shrink-0 rounded-full bg-slate-300 dark:bg-slate-700 sm:hidden" />

        {/* Encabezado del Bottom Sheet */}
        <div className="flex items-center justify-between border-b border-border pb-3 mb-4">
          <h3
            id="bottom-sheet-title"
            className="text-base font-bold tracking-tight text-foreground"
          >
            {title}
          </h3>
          <button
            type="button"
            onClick={onClose}
            aria-label="Cerrar ventana"
            className="rounded-lg p-1.5 text-muted-foreground hover:bg-accent hover:text-foreground transition"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Cuerpo del Bottom Sheet */}
        <div className="text-sm">{children}</div>
      </div>
    </div>
  );
}
