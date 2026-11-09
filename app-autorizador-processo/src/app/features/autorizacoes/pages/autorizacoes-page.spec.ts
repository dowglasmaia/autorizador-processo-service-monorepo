import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { errorInterceptor } from '../../../core/http/error.interceptor';
import { environment } from '../../../../environments/environment';
import type { AutorizacaoDto, PaginaAutorizacoes } from '../../../shared/api';
import { AutorizacoesPage } from './autorizacoes-page';

const URL = `${environment.apiBaseUrl.replace(/\/+$/, '')}/autorizacoes`;

const aut = (id: number, criadaEm?: string | null): AutorizacaoDto =>
  ({
    id,
    procedimento: {
      id: 1,
      numProcedimento: 4567,
      nome: id === 2 ? 'Ressonância' : undefined,
      sexos: ['M', 'F'],
      faixasEtarias: ['ADOLESCENTE', 'ADULTO'],
      criadoEm: '2026-01-01T00:00:00Z',
    },
    permitido: true,
    criadaEm,
  }) as AutorizacaoDto;

const pagina = (itens: AutorizacaoDto[], p = 0, total = 1): PaginaAutorizacoes => ({
  itens,
  pagina: p,
  tamanho: 20,
  totalItens: itens.length,
  totalPaginas: itens.length === 0 ? 0 : total,
});

describe('AutorizacoesPage', () => {
  let http: HttpTestingController;
  let router: Router;

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
    router = TestBed.inject(Router);
    const fixture = TestBed.createComponent(AutorizacoesPage);
    fixture.detectChanges();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  afterEach(() => http.verify());

  it('lista 2 linhas na ordem recebida, com status e data nula como "—"', async () => {
    const { fixture, el } = montar();
    http
      .expectOne((r) => r.url === URL)
      .flush(pagina([aut(2, '2026-01-02T10:00:00Z'), aut(1, null)]));
    await fixture.whenStable();
    const linhas = el.querySelectorAll('tbody tr');
    expect(linhas.length).toBe(2);
    expect(linhas[0].textContent).toContain('Ressonância');
    expect(linhas[0].textContent).toContain('Autorizado');
    expect(linhas[0].textContent).toContain('02/01/2026');
    expect(linhas[1].textContent).toContain('—');
    const ids = Array.from(linhas).map((l) => l.querySelector('td')?.textContent?.trim());
    expect(ids).toEqual(['2', '1']);
  });

  it('mostra o estado vazio com CTA para solicitar', async () => {
    const { fixture, el } = montar();
    http.expectOne((r) => r.url === URL).flush(pagina([]));
    await fixture.whenStable();
    expect(el.textContent).toContain('Nada por aqui ainda');
    const spy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    Array.from(el.querySelectorAll('button'))
      .find((b) => b.textContent?.includes('Solicitar a primeira'))
      ?.click();
    expect(spy).toHaveBeenCalledWith(['/solicitar']);
  });

  it('mostra o erro e tenta novamente', async () => {
    const { fixture, el } = montar();
    http.expectOne((r) => r.url === URL).flush(null, { status: 500, statusText: 'Erro' });
    await fixture.whenStable();
    expect(el.textContent).toContain('Não foi possível carregar');
    Array.from(el.querySelectorAll('button'))
      .find((b) => b.textContent?.includes('Tentar novamente'))
      ?.click();
    http.expectOne((r) => r.url === URL).flush(pagina([]));
  });

  it('troca de página com Anterior/Próxima e respeita os limites', async () => {
    const { fixture, el } = montar();
    http.expectOne((r) => r.url === URL).flush(pagina([aut(1, null)], 0, 2));
    await fixture.whenStable();
    const botao = (t: string) =>
      Array.from(el.querySelectorAll('nav button')).find((b) =>
        b.textContent?.includes(t),
      ) as HTMLButtonElement;
    expect(el.querySelector('[data-testid="pagina-atual"]')?.textContent).toContain(
      'Página 1 de 2',
    );
    expect(botao('Anterior').disabled).toBe(true);
    expect(botao('Próxima').disabled).toBe(false);
    botao('Próxima').click();
    const req = http.expectOne((r) => r.url === URL);
    expect(req.request.params.get('page')).toBe('1');
    req.flush(pagina([aut(1, null)], 1, 2));
    await fixture.whenStable();
    expect(el.querySelector('[data-testid="pagina-atual"]')?.textContent).toContain(
      'Página 2 de 2',
    );
    expect(botao('Próxima').disabled).toBe(true);
    expect(botao('Anterior').disabled).toBe(false);
  });
});
