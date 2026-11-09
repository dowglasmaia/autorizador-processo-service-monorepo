import { expect, test, type Page } from '@playwright/test';
import { entrar, irPara, numeroUnico } from './helpers';

async function avancar(page: Page): Promise<void> {
  await page.getByRole('button', { name: 'Avançar', exact: true }).click();
}

async function preencherIdentificacao(page: Page, numero: number, nome: string): Promise<void> {
  await page.getByLabel('Número do procedimento').fill(String(numero));
  await page.getByLabel('Nome do procedimento').fill(nome);
  await page.getByLabel('Categoria').selectOption('CONSULTAS');
}

test('Cenário 2 · cadastra um procedimento completo em 4 etapas e o vê na lista', async ({
  page,
}) => {
  const numero = numeroUnico();
  const nome = `Consulta e2e ${numero}`;
  await entrar(page);
  await irPara(page, 'Procedimentos');
  await page.getByRole('button', { name: 'Novo procedimento' }).first().click();
  await expect(page).toHaveURL(/\/procedimentos\/novo$/);

  // 1 · Identificação
  await preencherIdentificacao(page, numero, nome);
  await avancar(page);

  // 2 · Cobertura
  await page.getByLabel('Cobertura').selectOption('PLANO');
  await page.getByLabel('Quantidade máxima').fill('2');
  await page.getByLabel('Período').selectOption('MES');
  await avancar(page);

  // 3 · Elegibilidade
  await page.getByRole('button', { name: /^Adolescente/ }).click();
  await expect(page.getByRole('button', { name: /^Adolescente/ })).toHaveAttribute('aria-pressed', 'true');
  await page.getByRole('group', { name: 'Sexo' }).getByRole('button', { name: 'Feminino' }).click();
  await avancar(page);

  // 4 · Utilização e resumo
  await page.getByLabel('Exige autorização prévia').check();
  await expect(page.getByRole('heading', { name: 'Resumo' })).toBeVisible();
  await expect(page.locator('dl')).toContainText(String(numero));
  await page.getByRole('button', { name: 'Cadastrar procedimento' }).click();

  await expect(page).toHaveURL(/\/procedimentos$/);
  await expect(page.getByText(`Procedimento ${numero} cadastrado`).first()).toBeVisible();
  // A lista renderiza tabela (desktop) e cartões (móvel); só um dos dois fica visível.
  await expect(page.getByText(nome).locator('visible=true').first()).toBeVisible();
});

test('Cenário 3 · número duplicado mostra erro no campo', async ({ page }) => {
  await entrar(page, '/procedimentos/novo');
  // 1234 pertence à seed e nunca é alterado pelos cenários.
  await preencherIdentificacao(page, 1234, 'Duplicado e2e');
  await avancar(page);
  await avancar(page);
  await avancar(page);
  await page.getByRole('button', { name: 'Cadastrar procedimento' }).click();

  const campo = page.getByLabel('Número do procedimento');
  await expect(campo).toBeVisible();
  await expect(campo).toHaveAttribute('aria-invalid', 'true');
  await expect(page.getByText(/já existe/i).first()).toBeVisible();
  await expect(page).toHaveURL(/\/procedimentos\/novo$/);
});
