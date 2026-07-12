"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

export default function DashboardPage() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    const storedEmail = localStorage.getItem("userEmail");

    if (!token) {
      router.push("/login");
    } else {
      setEmail(storedEmail || "user@fintrack.com");
      setLoading(false);
    }
  }, [router]);

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("userEmail");
    router.push("/login");
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-zinc-950 font-sans text-zinc-200">
        <svg className="h-8 w-8 animate-spin text-indigo-500" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
        </svg>
      </div>
    );
  }

  return (
    <div className="relative min-h-screen bg-zinc-950 px-6 py-12 font-sans text-zinc-200">
      {/* Decorative Gradients */}
      <div className="absolute top-[-10%] right-[-10%] h-[500px] w-[500px] rounded-full bg-indigo-500/5 blur-[120px]"></div>
      <div className="absolute bottom-[-10%] left-[-10%] h-[500px] w-[500px] rounded-full bg-violet-500/5 blur-[120px]"></div>

      <div className="mx-auto max-w-5xl">
        {/* Header */}
        <header className="mb-12 flex items-center justify-between border-b border-zinc-800/80 pb-6">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-600/20 text-indigo-400 border border-indigo-500/30">
              <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <span className="text-xl font-bold tracking-tight text-white">FinTrack</span>
          </div>

          <div className="flex items-center gap-4">
            <span id="user-email-display" className="text-sm text-zinc-400">{email}</span>
            <button
              id="logout-btn"
              onClick={handleLogout}
              className="rounded-lg border border-zinc-800 bg-zinc-900/50 px-4 py-2 text-sm font-semibold text-zinc-300 transition hover:bg-zinc-800 hover:text-white"
            >
              Sign Out
            </button>
          </div>
        </header>

        {/* Dashboard Grid */}
        <main className="grid gap-6 md:grid-cols-3">
          {/* Card 1 */}
          <div className="rounded-2xl border border-zinc-800/80 bg-zinc-900/40 p-6 backdrop-blur-md">
            <div className="mb-4 flex items-center justify-between">
              <span className="text-sm font-medium text-zinc-400">Total Balance</span>
              <span className="rounded-full bg-emerald-500/10 px-2.5 py-0.5 text-xs font-semibold text-emerald-400">Active</span>
            </div>
            <div className="text-3xl font-bold text-white">$14,245.50</div>
            <div className="mt-2 text-xs text-zinc-500">+12% from last month</div>
          </div>

          {/* Card 2 */}
          <div className="rounded-2xl border border-zinc-800/80 bg-zinc-900/40 p-6 backdrop-blur-md">
            <div className="mb-4 flex items-center justify-between">
              <span className="text-sm font-medium text-zinc-400">Monthly Budget</span>
              <span className="rounded-full bg-indigo-500/10 px-2.5 py-0.5 text-xs font-semibold text-indigo-400">55% used</span>
            </div>
            <div className="text-3xl font-bold text-white">$2,500.00</div>
            <div className="mt-2 text-xs text-zinc-500">Remaining: $1,125.00</div>
          </div>

          {/* Card 3 */}
          <div className="rounded-2xl border border-zinc-800/80 bg-zinc-900/40 p-6 backdrop-blur-md">
            <div className="mb-4 flex items-center justify-between">
              <span className="text-sm font-medium text-zinc-400">System Security</span>
              <span className="rounded-full bg-indigo-500/10 px-2.5 py-0.5 text-xs font-semibold text-indigo-400">BFF Gateway</span>
            </div>
            <div className="text-3xl font-bold text-white">Protected</div>
            <div className="mt-2 text-xs text-zinc-500">JWT Token Validation active</div>
          </div>
        </main>

        <section className="mt-12 rounded-2xl border border-zinc-800/80 bg-zinc-900/20 p-8 text-center">
          <h3 className="text-lg font-semibold text-white">Welcome to the FinTrack Ecosistema!</h3>
          <p className="mx-auto mt-2 max-w-md text-sm text-zinc-400">
            You have successfully authenticated via the OAuth2/JWT auth-service. The Reverse Proxy BFF Gateway is intercepting your requests.
          </p>
        </section>
      </div>
    </div>
  );
}
