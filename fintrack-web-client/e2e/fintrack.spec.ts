import { test, expect } from '@playwright/test';

test.describe('FinTrack Web Client - E2E Test Suite', () => {

  test.beforeEach(async ({ page }) => {
    // Interceptar llamadas API del BFF para garantizar pruebas deterministas y reproducibles
    await page.route('**/api/v1/auth/login', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        headers: {
          'Set-Cookie': 'accessToken=mockJwtTokenValidForTesting; Path=/; HttpOnly; SameSite=Lax',
        },
        body: JSON.stringify({
          success: true,
          token: 'mockJwtTokenValidForTesting',
          user: 'usuario@fintrack.com',
        }),
      });
    });

    await page.route('**/api/v1/accounts/summary', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'acc-101',
          userId: 'user-001',
          accountNumber: 'ES9121000418450200051332',
          balance: 3450.75,
          currency: 'EUR',
        }),
      });
    });

    await page.route('**/api/v1/budgets', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { id: 'b-1', userId: 'user-001', category: 'ALIMENTACION', monthlyLimit: 450, accumulatedSpend: 120.5, percentageUsed: 26.7 },
          { id: 'b-2', userId: 'user-001', category: 'VIVIENDA', monthlyLimit: 900, accumulatedSpend: 850.0, percentageUsed: 94.4 },
          { id: 'b-3', userId: 'user-001', category: 'OCIO', monthlyLimit: 200, accumulatedSpend: 45.0, percentageUsed: 22.5 },
          { id: 'b-4', userId: 'user-001', category: 'TRANSPORTE', monthlyLimit: 150, accumulatedSpend: 30.0, percentageUsed: 20.0 },
          { id: 'b-5', userId: 'user-001', category: 'SALUD_DEPORTE', monthlyLimit: 120, accumulatedSpend: 15.0, percentageUsed: 12.5 },
          { id: 'b-6', userId: 'user-001', category: 'OTROS', monthlyLimit: 250, accumulatedSpend: 50.0, percentageUsed: 20.0 },
        ]),
      });
    });

    await page.route('**/api/v1/transactions', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { id: 'tx-1', amount: -45.5, currency: 'EUR', merchant: 'Mercadona Supermercado', status: 'CLEAN', category: 'ALIMENTACION', timestamp: '2026-08-14T10:00:00Z' },
          { id: 'tx-2', amount: -850.0, currency: 'EUR', merchant: 'Alquiler Vivienda Centro', status: 'CLEAN', category: 'VIVIENDA', timestamp: '2026-08-01T08:00:00Z' },
          { id: 'tx-3', amount: 2400.0, currency: 'EUR', merchant: 'Nómina Empresa Tech', status: 'CLEAN', category: 'OTROS', timestamp: '2026-08-01T07:00:00Z' },
          { id: 'tx-4', amount: -280.0, currency: 'EUR', merchant: 'Transferencia Rara Offshore', status: 'QUARANTINED', category: 'OTROS', timestamp: '2026-08-14T12:00:00Z' },
        ]),
      });
    });

    await page.route('**/api/v1/ingest/transaction', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          transactionId: 'tx-man-playwright-999',
          status: 'PENDING',
          merchant: 'Restaurante Prueba',
          amount: 32.5,
        }),
      });
    });

    await page.route('**/api/v1/events/stream', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'text/event-stream; charset=utf-8',
        headers: {
          'Cache-Control': 'no-cache',
          'Connection': 'keep-alive',
        },
        body: ': ping\n\n',
      });
    });
  });

  test('1. Verificación de la página de Login y redirección exitosa', async ({ page }) => {
    await page.goto('/login');

    // Comprobar elementos visuales y títulos
    await expect(page.locator('h1')).toContainText('Iniciar Sesión');
    await expect(page.locator('#email-input')).toBeVisible();
    await expect(page.locator('#password-input')).toBeVisible();
    await expect(page.locator('#login-submit-btn')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Google' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'GitHub' })).toBeVisible();

    // Rellenar credenciales e iniciar sesión
    await page.locator('#email-input').fill('usuario@fintrack.com');
    await page.locator('#password-input').fill('PasswordSeguro123!');
    await page.locator('#login-submit-btn').click();

    // Validar redirección hacia /dashboard
    await page.waitForURL('**/dashboard');
    await expect(page).toHaveURL(/\/dashboard/);
    await expect(page.locator('h1')).toContainText('FinTrack');
  });

  test('2. Verificación del Dashboard completo (Hero card, presupuestos, feed, accesos y modal)', async ({ page }) => {
    // Configurar estado de usuario previo en localStorage para acceder directamente al dashboard
    await page.addInitScript(() => {
      localStorage.setItem('userEmail', 'victor@fintrack.com');
    });

    await page.goto('/dashboard');

    // 2.1 Cabecera y badge de seguridad
    await expect(page.locator('h1')).toContainText('FinTrack');
    await expect(page.getByText('Protegido', { exact: false })).toBeVisible();
    await expect(page.locator('#user-email-display')).toContainText('victor');

    // 2.2 Hero Card (Balance Total y Gasto Mensual)
    await expect(page.getByText('Balance Total Disponible')).toBeVisible();
    await expect(page.getByText('Gasto del Mes')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Añadir Gasto' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Importar CSV' })).toBeVisible();

    // 2.3 Tarjetas de presupuestos por categoría
    const budgetSection = page.locator('section').filter({ hasText: 'Presupuestos por Categoría' });
    await expect(budgetSection).toBeVisible();
    await expect(budgetSection.getByText('Alimentación')).toBeVisible();
    await expect(budgetSection.getByText('Vivienda & Servicios')).toBeVisible();

    // Interactuar con expansión de tarjeta de categoría dentro de la sección de presupuestos
    const cardAlimentacion = budgetSection.locator('.ui-card').filter({ hasText: 'Alimentación' });
    await cardAlimentacion.click();
    await expect(cardAlimentacion.getByText('Ocultar').or(cardAlimentacion.getByText('Detalles'))).toBeVisible();

    // 2.4 Feed de Movimientos y pestañas de filtro
    const txSection = page.locator('section').filter({ hasText: 'Últimos Movimientos' });
    await expect(txSection).toBeVisible();
    await expect(txSection.getByRole('button', { name: /Todos/i })).toBeVisible();
    await expect(txSection.getByRole('button', { name: /Alertas/i })).toBeVisible();
    await expect(txSection.getByRole('button', { name: /Gastos/i })).toBeVisible();
    await expect(txSection.getByRole('button', { name: /Ingresos/i })).toBeVisible();

    // Validar presencia de movimientos cargados
    await expect(txSection.getByText('Mercadona Supermercado')).toBeVisible();

    // Filtrar por Gastos
    await txSection.getByRole('button', { name: /Gastos/i }).click();
    await expect(txSection.getByText('Mercadona Supermercado')).toBeVisible();

    // 2.5 Modal de Nuevo Gasto
    await page.getByRole('button', { name: 'Añadir Gasto' }).first().click();
    await expect(page.getByRole('heading', { name: 'Registrar Nuevo Gasto' })).toBeVisible();

    // Rellenar formulario del modal
    await page.getByPlaceholder('Ej. Supermercado, Gasolinera...').fill('Cena Restaurante');
    await page.getByPlaceholder('0.00').fill('45.00');
    await page.getByRole('button', { name: 'Guardar Transacción' }).click();

    // Validar que el nuevo gasto aparece reactivamente en el feed
    await expect(txSection.getByText('Cena Restaurante')).toBeVisible();
  });

  test('3. Verificación de cambio de tema visual (Dark / Light mode)', async ({ page }) => {
    await page.goto('/login');

    const themeToggleBtn = page.locator('#theme-toggle-btn');
    await expect(themeToggleBtn).toBeVisible();

    // Obtener estado inicial de la clase 'dark' en el elemento raíz <html>
    const isInitiallyDark = await page.evaluate(() => document.documentElement.classList.contains('dark'));

    // Alternar tema
    await themeToggleBtn.click();

    // Verificar que la clase en <html> se invirtió
    const isDarkAfterFirstToggle = await page.evaluate(() => document.documentElement.classList.contains('dark'));
    expect(isDarkAfterFirstToggle).toBe(!isInitiallyDark);

    // Verificar que el tema se persiste en localStorage
    const savedTheme = await page.evaluate(() => localStorage.getItem('theme'));
    expect(savedTheme).toBe(isDarkAfterFirstToggle ? 'dark' : 'light');

    // Alternar nuevamente para regresar al estado previo
    await themeToggleBtn.click();
    const isDarkAfterSecondToggle = await page.evaluate(() => document.documentElement.classList.contains('dark'));
    expect(isDarkAfterSecondToggle).toBe(isInitiallyDark);
  });

});
