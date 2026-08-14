import { finTrackApi } from "@/lib/api";

/**
 * Pruebas unitarias para el módulo cliente HTTP finTrackApi.
 */
describe("finTrackApi - HTTP Client Unit Tests", () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test("getAccountSummary invoca /api/v1/accounts/summary con Content-Type application/json", async () => {
    const mockSummary = {
      id: "acc-1",
      userId: "user-1",
      accountNumber: "ES00-1234",
      balance: 14550.8,
      currency: "EUR",
    };

    (global.fetch as jest.Mock).mockResolvedValue({
      ok: true,
      json: async () => mockSummary,
    });

    const result = await finTrackApi.getAccountSummary();

    expect(global.fetch).toHaveBeenCalledWith("/api/v1/accounts/summary", {
      headers: {
        "Content-Type": "application/json",
      },
    });
    expect(result).toEqual(mockSummary);
  });

  test("getBudgets retorna el listado de presupuestos", async () => {
    const mockBudgets = [
      { id: "b-1", userId: "u-1", category: "ALIMENTACION", monthlyLimit: 450, accumulatedSpend: 340, percentageUsed: 75 },
    ];

    (global.fetch as jest.Mock).mockResolvedValue({
      ok: true,
      json: async () => mockBudgets,
    });

    const result = await finTrackApi.getBudgets();

    expect(global.fetch).toHaveBeenCalledWith("/api/v1/budgets", {
      headers: {
        "Content-Type": "application/json",
      },
    });
    expect(result).toEqual(mockBudgets);
  });

  test("lanza un error adecuado cuando la API retorna 401 Unauthorized", async () => {
    (global.fetch as jest.Mock).mockResolvedValue({
      ok: false,
      status: 401,
      statusText: "Unauthorized",
      json: async () => ({ message: "Sesión no válida o expirada" }),
    });

    await expect(finTrackApi.getAccountSummary()).rejects.toThrow(
      "Sesión no válida o expirada"
    );
  });
});

