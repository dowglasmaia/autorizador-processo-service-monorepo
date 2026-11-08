import { expect, test } from '@playwright/test';
import { compose } from './ambiente';
import { entrar } from './helpers';

test.describe('Resiliência e responsividade', () => {
  test('Cenário 6 · ?numero= inexistente forjado não pré-seleciona e trata o erro', async ({
    page,
  }) => {
    await entrar(page);
    await page.goto('/solicitar?numero=999999999');
    const select = page.getByRole('combobox', { name: 'Procedimento', exact: true });
    await expect(select).toBeVisible();
    // Nada é pré-selecionado: a opção "Selecione" permanece.
    await expect(select.locator('option:checked')).toHaveText('Selecione');

    // Enviar sem escolher mostra o erro de validação tratado, sem quebrar a tela.
    await page.getByRole('button', { name: 'Solicitar autorização' }).click();
    await expect(page.getByText(/Escolha um procedimento/i).first()).toBeVisible();
    await expect(page.locator('[data-resultado="inicial"]')).toBeVisible();
  });

  test('Cenário 7 · API parada mostra "Não foi possível carregar" e a tela retoma ao religar', async ({
    page,
  }) => {
    await entrar(page);
    compose('stop', 'api');
    try {
      await page.goto('/procedimentos');
      await expect(page.getByText('Não foi possível carregar', { exact: true })).toBeVisible({
        timeout: 20_000,
      });
    } finally {
      compose('up', '-d', '--wait', '--wait-timeout', '240', 'api');
    }
    // Após religar, "Tentar novamente" recarrega a lista (o Nginx pode precisar de alguns segundos).
    await expect(async () => {
      await page.getByRole('button', { name: 'Tentar novamente' }).click({ timeout: 2_000 });
      await expect(page.getByText('Não foi possível carregar', { exact: true })).toHaveCount(0, {
        timeout: 3_000,
      });
    }).toPass({ timeout: 60_000 });
    // Tabela no desktop e cartões no móvel: basta um procedimento da seed visível.
    await expect(page.getByText('1234').locator('visible=true').first()).toBeVisible();
  });

  test('Cenário 8 · rota SPA direta (/autorizacoes) funciona após reload', async ({ page }) => {
    await entrar(page, '/autorizacoes');
    await page.reload();
    await expect(page).toHaveURL(/\/autorizacoes$/);
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
  });

  test('Cenário 9 · viewport de 320 px sem rolagem horizontal nas telas principais', async ({
    page,
  }) => {
    await page.setViewportSize({ width: 320, height: 640 });
    await entrar(page);
    for (const rota of ['/painel', '/procedimentos', '/solicitar', '/autorizacoes']) {
      await page.goto(rota);
      await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
      const sobra = await page.evaluate(
        () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
      );
      expect(sobra, `rolagem horizontal em ${rota}`).toBeLessThanOrEqual(0);
    }
  });
});
