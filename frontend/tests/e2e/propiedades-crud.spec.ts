import { test, expect } from '@playwright/test';

import { loginAsAdmin, waitForSnackbar } from './support/test-helpers';

test.describe('Property management', () => {
  test('performs Propiedades CRUD flow end-to-end', async ({ page }) => {
    const timestamp = Date.now();
    const property = {
      name: `Test Property ${timestamp}`,
      address: '123 Test Street, Example City',
      initialNotes: 'Initial automated test notes',
      updatedNotes: 'Updated automated notes',
    };

    await loginAsAdmin(page);

    await page.goto('/propiedades');
    await expect(page.getByRole('heading', { name: /property management/i })).toBeVisible();

    await page.getByRole('button', { name: /new property/i }).click();
    await expect(page.getByRole('heading', { name: /new property/i })).toBeVisible();

    await page.getByRole('textbox', { name: /name/i }).fill(property.name);
    await page.getByRole('textbox', { name: /address/i }).fill(property.address);
    await page.getByRole('textbox', { name: /notes/i }).fill(property.initialNotes);

    await page.getByLabel(/^type$/i).click();
    await page.getByRole('option', { name: /apartment/i }).click();

    await page.getByLabel(/^agency$/i).click();
    await page.getByRole('option', { name: /inmobiliaria del centro/i }).click();

    await page.getByRole('button', { name: /^save$/i }).click();

    await waitForSnackbar(page, /record saved successfully/i);

    const propertyRow = page.locator('.MuiDataGrid-row', { hasText: property.name }).first();
    await expect(propertyRow).toBeVisible();
    await expect(propertyRow).toContainText('Apartment');
    await expect(propertyRow).toContainText(property.initialNotes);

    await propertyRow.locator('button[aria-label="Edit"]').click();
    await expect(page.getByRole('heading', { name: /edit property/i })).toBeVisible();

    await page.getByRole('textbox', { name: /notes/i }).fill(property.updatedNotes);
    await page.getByRole('button', { name: /^save$/i }).click();

    await waitForSnackbar(page, /record saved successfully/i);
    await expect(propertyRow).toContainText(property.updatedNotes);

    await propertyRow.locator('button[aria-label="Delete"]').click();
    await expect(page.getByText(/are you sure you want to delete this record/i)).toBeVisible();
    await page.getByRole('button', { name: /^delete$/i }).click();

    await waitForSnackbar(page, /record deleted successfully/i);
    await expect(page.locator('.MuiDataGrid-row', { hasText: property.name })).toHaveCount(0);
  });
});
