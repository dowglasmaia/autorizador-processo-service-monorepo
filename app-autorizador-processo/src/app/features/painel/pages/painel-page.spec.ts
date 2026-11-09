import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { errorInterceptor } from '../../../core/http/error.interceptor';
import { SessaoSolicitacoes } from '../../../core/sessao/sessao-solicitacoes';
import { environment } from '../../../../environments/environment';
import type { ResumoPainel } from '../../../shared/api';
import { PainelPage } from './painel-page';

const URL = `${environment.apiBaseUrl.replace(/\/+$/, '')}/painel/resumo`;

const seed: ResumoPainel = {
  totalProcedimentos: 3,
  totalAutorizacoes: 0,
  faixasCobertas: 3,
  coberturaPorFaixa: [
    { faixa: 'CRIANCA', quantidade: 2 },
    { faixa: 'ADOLESCENTE', quantidade: 2 },
    { faixa: 'ADULTO', quantidade: 1 },
  ],
  distribuicaoSexos: { masculino: 2, feminino: 3 },
  ultimasAutorizacoes: [],
};

describe('PainelPage', () => {
  let http: HttpTestingController;

  function montar() {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(PainelPage);
    fixture.detectChanges();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  const valoresKpi = (el: HTMLElement) =>
    Array.from(el.querySelectorAll('app-kpi-card')).map((k) =>
      k.querySelector('p.text-3xl')?.textContent?.trim(),
    );

  afterEach(() => http.verify());

  it('mostra esqueleto enquanto carrega', () => {
    const { el } = montar();
    expect(el.querySelectorAll('app-skeleton').length).toBeGreaterThan(0);
    http.expectOne(URL).flush(seed);
  });

  it('seed: KPIs 3/0/3/"—", barras, rosca e vazio', async () => {
    const { fixture, el } = montar();
    http.expectOne(URL).flush(seed);
    await fixture.whenStable();
    expect(valoresKpi(el)).toEqual(['3', '0', '3', '—']);
    expect(el.textContent).toContain('solicitações desta sessão');
    expect(el.querySelectorAll('[data-testid="barra"]').length).toBe(3);
    expect(el.textContent).toContain('Masculino: 2');
    expect(el.textContent).toContain('Feminino: 3');
    expect(el.textContent).toContain('Nenhuma autorização concedida ainda.');
  });

  it('uma concedida na sessão: Aprovação 100%', async () => {
    const { fixture, el } = montar();
    TestBed.inject(SessaoSolicitacoes).registrar('autorizado');
    http.expectOne(URL).flush(seed);
    await fixture.whenStable();
    expect(valoresKpi(el)[3]).toBe('100%');
  });

  it('erro de rede mostra "Tentar novamente" e recarrega', async () => {
    const { fixture, el } = montar();
    http.expectOne(URL).error(new ProgressEvent('error'));
    await fixture.whenStable();
    const botao = Array.from(el.querySelectorAll('button')).find((b) =>
      b.textContent?.includes('Tentar novamente'),
    );
    expect(botao).toBeDefined();
    botao?.click();
    http.expectOne(URL).flush(seed);
    await fixture.whenStable();
    expect(el.textContent).not.toContain('Tentar novamente');
  });
});
