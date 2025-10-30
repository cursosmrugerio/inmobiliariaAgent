import { test, expect } from '@playwright/test';

import { loginAsAdmin, waitForSnackbar } from './support/test-helpers';

test.describe('Agency management', () => {
  test('performs Inmobiliarias CRUD flow end-to-end', async ({ page }) => {
    const timestamp = Date.now();
    const agency = {
      name: `Agencia Playwright ${timestamp}`,
      rfc: `RFC${`${timestamp}`.slice(-9)}`,
      contact: 'Laura QA',
      email: `qa.agency.${timestamp}@example.com`,
      phoneInitial: '555-7777',
      phoneUpdated: '555-8888',
    };

    await loginAsAdmin(page);

    await page.goto('/inmobiliarias');
    await expect(page.getByRole('heading', { name: /agency management/i })).toBeVisible();

    await page.getByRole('button', { name: /new agency/i }).click();
    await expect(page.getByRole('heading', { name: /new agency/i })).toBeVisible();

    await page.getByRole('textbox', { name: /^name$/i }).fill(agency.name);
    await page.getByRole('textbox', { name: /tax id/i }).fill(agency.rfc);
    await page.getByRole('textbox', { name: /contact name/i }).fill(agency.contact);
    await page.getByRole('textbox', { name: /^email$/i }).fill(agency.email);
    await page.getByRole('textbox', { name: /^phone$/i }).fill(agency.phoneInitial);
    await page.getByRole('button', { name: /^save$/i }).click();

    await waitForSnackbar(page, /record saved successfully/i);

    const agencyRow = page.locator('.MuiDataGrid-row', { hasText: agency.name }).first();
    await expect(agencyRow).toBeVisible();
    await expect(agencyRow).toContainText(agency.phoneInitial);

    await agencyRow.locator('button[aria-label="Edit"]').click();
    await expect(page.getByRole('heading', { name: /edit agency/i })).toBeVisible();

    await page.getByRole('textbox', { name: /^phone$/i }).fill(agency.phoneUpdated);
    await page.getByRole('button', { name: /^save$/i }).click();

    await waitForSnackbar(page, /record saved successfully/i);
    await expect(agencyRow).toContainText(agency.phoneUpdated);

    await agencyRow.locator('button[aria-label="Delete"]').click();
    await expect(page.getByText(/are you sure you want to delete this record/i)).toBeVisible();
    await page.getByRole('button', { name: /^delete$/i }).click();

    await waitForSnackbar(page, /record deleted successfully/i);
    await expect(page.locator('.MuiDataGrid-row', { hasText: agency.name })).toHaveCount(0);
  });
});
