import { expect, test, type Locator, type Page } from '@playwright/test';
import { SEED, entrar, lerResumoDaApi } from './helpers';

function valorDoIndicador(page: Page, rotulo: string): Locator {
  return page
    .locator('app-kpi-card')
    .filter({ has: page.getByText(rotulo, { exact: true }) })
    .locator('p.text-3xl');
}

test('Cenário 1 · o painel mostra os totais da seed', async ({ page, request }, testInfo) => {
  await entrar(page);
  const resumo = await lerResumoDaApi(request);

  // Os projetos rodam em série no mesmo banco: o primeiro (desktop) vê a seed intacta;
  // os seguintes veem a seed mais o que os cenários de escrita já criaram.
  if (testInfo.project.name === 'desktop') {
    expect(resumo).toMatchObject({
      totalProcedimentos: SEED.totalProcedimentos,
      totalAutorizacoes: SEED.totalAutorizacoes,
      idadesCobertas: SEED.idadesCobertas,
    });
  } else {
    expect(resumo.totalProcedimentos).toBeGreaterThanOrEqual(SEED.totalProcedimentos);
  }

  await expect(valorDoIndicador(page, 'Procedimentos')).toHaveText(
    String(resumo.totalProcedimentos),
  );
  await expect(valorDoIndicador(page, 'Autorizações')).toHaveText(
    String(resumo.totalAutorizacoes),
  );
  await expect(valorDoIndicador(page, 'Idades cobertas')).toHaveText(
    String(resumo.idadesCobertas),
  );
});
