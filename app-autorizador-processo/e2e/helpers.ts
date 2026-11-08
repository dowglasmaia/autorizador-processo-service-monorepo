import { expect, type APIRequestContext, type Page } from '@playwright/test';

/** Dados que a seed local (`db/seed/V1000__seed_local.sql`) carrega no ambiente e2e. */
export const SEED = {
  totalProcedimentos: 3,
  totalAutorizacoes: 0,
  idadesCobertas: 3,
  /** 1234: F, idades 10/20 · 4567: M/F, idades 20/30 · 6789: M/F, idade 10. */
  procedimentos: [1234, 4567, 6789],
} as const;

export interface ResumoPainelApi {
  totalProcedimentos: number;
  totalAutorizacoes: number;
  idadesCobertas: number;
}

export function credenciais(): { email: string; senha: string } {
  const email = process.env['E2E_EMAIL'];
  const senha = process.env['E2E_SENHA'];
  if (!email || !senha) {
    throw new Error('E2E_EMAIL/E2E_SENHA ausentes: o global setup não rodou.');
  }
  return { email, senha };
}

/** Preenche o formulário de login pela interface e envia. */
export async function preencherLogin(page: Page, email: string, senha: string): Promise<void> {
  await page.getByLabel('E-mail').fill(email);
  await page.getByRole('textbox', { name: 'Senha', exact: true }).fill(senha);
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();
}

/** Login pela UI com as credenciais do bootstrap; termina no destino pedido (padrão: painel). */
export async function entrar(page: Page, destino = '/painel'): Promise<void> {
  const { email, senha } = credenciais();
  await page.goto('/login');
  await preencherLogin(page, email, senha);
  await expect(page).toHaveURL(/\/painel$/);
  if (destino !== '/painel') {
    // `goto` recarrega a página; a sessão fica em sessionStorage e sobrevive ao reload.
    await page.goto(destino);
    await expect(page).toHaveURL(new RegExp(`${destino}$`));
  }
}

async function abrirMenuLateralSeNecessario(page: Page): Promise<void> {
  const abrirMenu = page.getByRole('button', { name: 'Abrir menu' });
  if (await abrirMenu.isVisible()) {
    await abrirMenu.click();
  }
}

/** Abre o menu do usuário (no móvel, antes abre o menu lateral) e sai da sessão. */
export async function sair(page: Page): Promise<void> {
  await abrirMenuLateralSeNecessario(page);
  await page.getByRole('button', { name: credenciais().email }).click();
  await page.getByRole('button', { name: 'Sair', exact: true }).click();
}

/** Navega por um item do menu principal (abre o menu lateral no móvel). */
export async function irPara(page: Page, item: string): Promise<void> {
  await abrirMenuLateralSeNecessario(page);
  await page
    .getByRole('navigation', { name: 'Principal' })
    .getByRole('link', { name: item, exact: true })
    .click();
}

let sequencia = 0;

/** Número de procedimento único por execução (cabe em int32 e não colide com a seed). */
export function numeroUnico(): number {
  sequencia += 1;
  return Number(String(Date.now()).slice(-8)) * 10 + (sequencia % 10) + 1000;
}

/** Lê o resumo do painel direto da API (mesma origem, via Nginx) para comparar com a tela. */
export async function lerResumoDaApi(request: APIRequestContext): Promise<ResumoPainelApi> {
  const { email, senha } = credenciais();
  const login = await request.post('/api/v1/auth/login', { data: { email, senha } });
  expect(login.ok()).toBeTruthy();
  const { accessToken } = (await login.json()) as { accessToken: string };
  const resposta = await request.get('/api/v1/painel/resumo', {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  expect(resposta.ok()).toBeTruthy();
  return (await resposta.json()) as ResumoPainelApi;
}
