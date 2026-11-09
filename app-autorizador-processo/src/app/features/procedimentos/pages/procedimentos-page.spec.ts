import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { errorInterceptor } from '../../../core/http/error.interceptor';
import { environment } from '../../../../environments/environment';
import type { PaginaProcedimentos } from '../../../shared/api';
import { ProcedimentosPage, resumoFaixas } from './procedimentos-page';

const URL = `${environment.apiBaseUrl.replace(/\/+$/, '')}/procedimentos`;

const pagina = (itens: PaginaProcedimentos['itens']): PaginaProcedimentos => ({
  itens,
  pagina: 0,
  tamanho: 20,
  totalItens: itens.length,
  totalPaginas: itens.length === 0 ? 0 : 1,
});

describe('resumoFaixas', () => {
  it('resume as faixas etárias e vazio', () => {
    expect(resumoFaixas([])).toBe('—');
    expect(resumoFaixas(['IDOSO'])).toBe('Idoso');
    expect(resumoFaixas(['CRIANCA', 'ADULTO'])).toBe('Criança, Adulto');
  });
});

describe('ProcedimentosPage', () => {
  let http: HttpTestingController;
  let router: Router;

  function montar(): {
    fixture: ReturnType<typeof TestBed.createComponent<ProcedimentosPage>>;
    el: HTMLElement;
  } {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    const fixture = TestBed.createComponent(ProcedimentosPage);
    fixture.detectChanges();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  afterEach(() => http.verify());

  it('mostra o esqueleto enquanto carrega', () => {
    const { el } = montar();
    expect(el.querySelector('[aria-label="Carregando procedimentos"]')).not.toBeNull();
    http.expectOne((r) => r.url === URL).flush(pagina([]));
  });

  it('mostra o estado vazio com CTA que leva ao cadastro', async () => {
    const { fixture, el } = montar();
    http.expectOne((r) => r.url === URL).flush(pagina([]));
    await fixture.whenStable();
    expect(el.textContent).toContain('Nenhum procedimento cadastrado');
    const spy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const botoes = Array.from(el.querySelectorAll('button')).filter((b) =>
      b.textContent?.includes('Novo procedimento'),
    );
    expect(botoes.length).toBe(2);
    botoes[1].click();
    expect(spy).toHaveBeenCalledWith(['/procedimentos', 'novo']);
  });

  it('mostra o erro e tenta novamente', async () => {
    const { fixture, el } = montar();
    http.expectOne((r) => r.url === URL).flush(null, { status: 500, statusText: 'Erro' });
    await fixture.whenStable();
    expect(el.textContent).toContain('Não foi possível carregar');
    const retry = Array.from(el.querySelectorAll('button')).find((b) =>
      b.textContent?.includes('Tentar novamente'),
    );
    retry?.click();
    http.expectOne((r) => r.url === URL).flush(pagina([]));
  });

  it('lista os itens, com "Sem nome" e o link "Solicitar →"', async () => {
    const { fixture, el } = montar();
    http
      .expectOne((r) => r.url === URL)
      .flush(
        pagina([
          {
            id: 1,
            numProcedimento: 9999,
            nome: 'Ressonância',
            categoria: 'EXAMES_IMAGEM',
            sexos: ['M', 'F'],
            faixasEtarias: ['ADOLESCENTE'],
            criadoEm: '2026-01-01T00:00:00Z',
          },
          { id: 2, numProcedimento: 10, sexos: [], faixasEtarias: [], criadoEm: '2026-01-01T00:00:00Z' },
        ]),
      );
    await fixture.whenStable();
    const tabela = el.querySelector('table') as HTMLTableElement;
    expect(tabela.querySelectorAll('tbody tr').length).toBe(2);
    expect(tabela.textContent).toContain('Ressonância');
    expect(tabela.textContent).toContain('Exames de imagem');
    expect(tabela.textContent).toContain('Adolescente');
    expect(tabela.textContent).toContain('Sem nome');
    const links = Array.from(tabela.querySelectorAll('a'));
    expect(links.map((a) => a.textContent?.trim())).toEqual(['Solicitar →', 'Solicitar →']);
    expect(links[0].getAttribute('href')).toBe('/solicitar?numero=9999');
    expect(links[1].getAttribute('href')).toBe('/solicitar?numero=10');
  });
});
