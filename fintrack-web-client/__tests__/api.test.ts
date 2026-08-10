import { finTrackApi } from "@/lib/api";

/**
 * Pruebas unitarias para el módulo cliente HTTP finTrackApi.
 */
describe("finTrackApi - HTTP Client Unit Tests", () => {
  beforeEach(() => {
    // Limpiar mocks e inyectar un token de prueba en localStorage
    global.fetch = jest.fn();
    Storage.prototype.getItem = jest.fn((key: string) => {
      if (key === "accessToken") return "mock-jwt-bearer-token";
      return null;
    });
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test("getAccountSummary adjunta el header Authorization y retorna el JSON esperado", async () => {
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
        Authorization: "Bearer mock-jwt-bearer-token",
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
        Authorization: "Bearer mock-jwt-bearer-token",
      },
    });
    expect(result).toEqual(mockBudgets);
  });

  test("lanza un error cuando la API retorna 401 Unauthorized", async () => {
    (global.fetch as jest.Mock).mockResolvedValue({
      ok: false,
      statusText: "Unauthorized",
    });

    await expect(finTrackApi.getAccountSummary()).rejects.toThrow(
      "Error al obtener el resumen de cuenta: Unauthorized"
    );
  });
});
