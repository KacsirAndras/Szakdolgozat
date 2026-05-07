import { expect, test } from '@playwright/test';

test('opens the localhost app and shows the login page', async ({ page }) => {
  await page.goto('/');

  await expect(page).toHaveURL(/\/login$/);
  await expect(page).toHaveTitle('Management Login');
  await expect(page.getByRole('heading', { name: /bejelentkezes|login/i })).toBeVisible();
  await expect(page.getByLabel(/email/i)).toBeVisible();
  await expect(page.locator('#jelszo')).toBeVisible();
});
