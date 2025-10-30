import { test, expect, type Page } from '@playwright/test';

const ADMIN_EMAIL = process.env.PLAYWRIGHT_ADMIN_EMAIL ?? 'admin@test.com';
const ADMIN_PASSWORD = process.env.PLAYWRIGHT_ADMIN_PASSWORD ?? 'admin123';

const waitForSnackbar = async (page: Page, message: RegExp) => {
  const snackbar = page.getByRole('alert').filter({ hasText: message });
  await expect(snackbar).toBeVisible();
};

test.describe('Personas management', () => {
  test('performs Personas CRUD flow end-to-end', async ({ page }) => {
    const timestamp = Date.now();
    const persona = {
      firstName: `Test${timestamp}`,
      lastName: 'User',
      email: `test.user.${timestamp}@example.com`,
      rfc: `TST${`${timestamp}`.slice(-9)}`,
      initialPhone: '555-0000',
      updatedPhone: '555-9999',
    };
    const personaFullName = `${persona.firstName} ${persona.lastName}`;

    await page.goto('/login');
    await expect(page.getByRole('heading', { name: /log in/i })).toBeVisible();

    await page.getByLabel(/email address/i).fill(ADMIN_EMAIL);
    await page.getByLabel(/password/i).fill(ADMIN_PASSWORD);
    await page.getByRole('button', { name: /log in/i }).click();
    await page.waitForURL('**/dashboard');

    await page.goto('/personas');
    await expect(page.getByRole('heading', { name: /contact management/i })).toBeVisible();

    await page.getByRole('button', { name: /new contact/i }).click();
    await expect(page.getByRole('heading', { name: /new contact/i })).toBeVisible();

    await page.getByRole('textbox', { name: /first name/i }).fill(persona.firstName);
    await page.getByRole('textbox', { name: /last name/i }).fill(persona.lastName);
    await page.getByRole('textbox', { name: /tax id \\(rfc\\)/i }).fill(persona.rfc);
    await page.getByRole('textbox', { name: /^email$/i }).fill(persona.email);
    await page.getByRole('textbox', { name: /^phone$/i }).fill(persona.initialPhone);
    await page.getByRole('button', { name: /^save$/i }).click();

    await waitForSnackbar(page, /record saved successfully/i);

    const personasRow = page.locator('.MuiDataGrid-row', { hasText: personaFullName }).first();
    await expect(personasRow).toBeVisible();
    await expect(personasRow).toContainText(persona.initialPhone);

    await personasRow.locator('button[aria-label="Edit"]').click();
    await expect(page.getByRole('heading', { name: /edit contact/i })).toBeVisible();

    await page.getByRole('textbox', { name: /^phone$/i }).fill(persona.updatedPhone);
    await page.getByRole('button', { name: /^save$/i }).click();

    await waitForSnackbar(page, /record saved successfully/i);
    await expect(personasRow).toContainText(persona.updatedPhone);

    await personasRow.locator('button[aria-label="Delete"]').click();
    await expect(page.getByText(/are you sure you want to delete this record/i)).toBeVisible();
    await page.getByRole('button', { name: /^delete$/i }).click();

    await waitForSnackbar(page, /record deleted successfully/i);
    await expect(page.locator('.MuiDataGrid-row', { hasText: personaFullName })).toHaveCount(0);
  });
});
