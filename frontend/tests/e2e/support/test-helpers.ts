import { expect, type Page } from '@playwright/test';

export const ADMIN_EMAIL = process.env.PLAYWRIGHT_ADMIN_EMAIL ?? 'admin@test.com';
export const ADMIN_PASSWORD = process.env.PLAYWRIGHT_ADMIN_PASSWORD ?? 'admin123';

export const loginAsAdmin = async (page: Page) => {
  await page.goto('/login');
  await expect(page.getByRole('heading', { name: /log in/i })).toBeVisible();

  await page.getByLabel(/email address/i).fill(ADMIN_EMAIL);
  await page.getByLabel(/password/i).fill(ADMIN_PASSWORD);
  await page.getByRole('button', { name: /log in/i }).click();
  await page.waitForURL('**/dashboard');
};

export const waitForSnackbar = async (page: Page, message: RegExp) => {
  const snackbar = page.getByRole('alert').filter({ hasText: message });
  await expect(snackbar).toBeVisible();
};
