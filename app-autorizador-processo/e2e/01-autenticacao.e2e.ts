import { expect, test } from '@playwright/test';
import { credenciais, entrar, preencherLogin, sair } from './helpers';

test.describe('Cenário 0 · autenticação', () => {
  test('login inválido mostra erro e permanece em /login', async ({ page }) => {
    await page.goto('/login');
    await preencherLogin(page, credenciais().email, 'senha-incorreta-123');
    await expect(page.getByRole('alert')).toContainText('E-mail ou senha incorretos.');
    await expect(page).toHaveURL(/\/login/);
  });

  test('login válido entra no painel', async ({ page }) => {
    await entrar(page);
    await expect(page.getByRole('heading', { name: 'Painel', level: 1 })).toBeVisible();
  });

  test('rota protegida sem sessão vai a /login com retorno e volta após entrar', async ({
    page,
  }) => {
    await page.goto('/autorizacoes');
    await expect(page).toHaveURL(/\/login\?retorno=(%2F|\/)autorizacoes$/);
    const { email, senha } = credenciais();
    await preencherLogin(page, email, senha);
    await expect(page).toHaveURL(/\/autorizacoes$/);
  });

  test('sair encerra a sessão e bloqueia as rotas protegidas', async ({ page }) => {
    await entrar(page);
    await sair(page);
    await expect(page).toHaveURL(/\/login/);
    await page.goto('/painel');
    await expect(page).toHaveURL(/\/login/);
  });
});

test.describe('Cenário 0b · sessão expirada', () => {
  test('o token de TTL curto expira e a tela volta ao login', async ({ page }) => {
    // O .env de teste usa JWT_TTL_MINUTOS=1; o app agenda o logout pelo relógio real (~60 s).
    test.setTimeout(150_000);
    await entrar(page);
    await expect(page).toHaveURL(/\/login/, { timeout: 100_000 });
    await expect(page.getByRole('status').filter({ hasText: 'Sua sessão expirou' })).toBeVisible();
  });
});
