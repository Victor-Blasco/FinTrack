import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import React from "react";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

/**
 * Metadatos globales SEO para la aplicación cliente FinTrack.
 */
export const metadata: Metadata = {
  title: "FinTrack & FraudShield — Control Financiero Inteligente",
  description: "Plataforma de gestión de presupuestos personales y protección de transacciones en tiempo real.",
};

/**
 * Componente de diseño raíz (Root Layout) para Next.js App Router.
 * <p>
 * Inyecta las fuentes de Google Geist Sans y Geist Mono, los estilos globales CSS
 * y define el contenedor HTML estructurado con {@code suppressHydrationWarning} para evitar advertencias de React.
 * </p>
 *
 * @param props propiedades del componente incluyendo nodos hijos {@link Readonly<{ children: React.ReactNode }>}
 * @returns elemento JSX de la plantilla raíz
 */
export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="es"
      suppressHydrationWarning
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col">{children}</body>
    </html>
  );
}
