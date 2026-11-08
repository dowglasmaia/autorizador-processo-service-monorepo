import { expect, test, type Page } from '@playwright/test';
import { entrar, irPara, lerResumoDaApi } from './helpers';

/** Preenche e envia a solicitação; o procedimento é escolhido pelo rótulo "Nº <número> · ...". */
async function solicitar(
  page: Page,
  procedimento: number,
  idade: number,
  sexo: 'Masculino' | 'Feminino',
): Promise<void> {
  const select = page.getByRole('combobox', { name: 'Procedimento', exact: true });
  const opcao = select.locator('option', { hasText: new RegExp(`^\\s*Nº ${procedimento}\\b`) });
  const rotulo = (await opcao.first().textContent())?.trim();
  expect(rotulo, `procedimento ${procedimento} na lista`).toBeTruthy();
  await select.selectOption({ label: rotulo as string });
  await page.getByLabel('Idade', { exact: false }).first().fill(String(idade));
  await page.getByRole('group', { name: 'Sexo' }).getByRole('button', { name: sexo }).click();
  await page.getByRole('button', { name: 'Solicitar autorização' }).click();
}

test('Cenário 4 · 4567/20/F é autorizado e aparece no histórico e no painel', async ({
  page,
  request,
}) => {
  await entrar(page, '/solicitar');
  const antes = await lerResumoDaApi(request);
  await solicitar(page, 4567, 20, 'Feminino');
  await expect(page.locator('[data-resultado="autorizado"]')).toContainText('Autorizado');

  await page.getByRole('link', { name: 'Ver autorizações concedidas' }).click();
  await expect(page).toHaveURL(/\/autorizacoes$/);
  // Histórico: mais recentes primeiro, então a nova autorização está na primeira página.
  await expect(page.getByText('Autorizado', { exact: true }).locator('visible=true').first()).toBeVisible();
  await expect(page.getByText(/4567/).locator('visible=true').first()).toBeVisible();

  const depois = await lerResumoDaApi(request);
  expect(depois.totalAutorizacoes).toBe(antes.totalAutorizacoes + 1);

  await irPara(page, 'Painel');
  await expect(
    page
      .locator('app-kpi-card')
      .filter({ has: page.getByText('Autorizações', { exact: true }) })
      .locator('p.text-3xl'),
  ).toHaveText(String(depois.totalAutorizacoes));
  await expect(page.getByText(/Procedimento 4567/).first()).toBeVisible();
});

test('Cenário 5 · 1234/30/M é negado e o histórico não muda', async ({ page, request }) => {
  await entrar(page, '/solicitar');
  const antes = await lerResumoDaApi(request);
  await solicitar(page, 1234, 30, 'Masculino');
  await expect(page.locator('[data-resultado="negado"]')).toContainText('Negado');

  const depois = await lerResumoDaApi(request);
  expect(depois.totalAutorizacoes).toBe(antes.totalAutorizacoes);

  await irPara(page, 'Autorizações');
  await expect(page).toHaveURL(/\/autorizacoes$/);
  await expect(page.getByText(/1234/)).toHaveCount(0);
});
