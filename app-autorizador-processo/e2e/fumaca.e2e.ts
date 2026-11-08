import { expect, test } from '@playwright/test';

test('a aplicação responde no ambiente e2e', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveURL(/\/login/);
});
