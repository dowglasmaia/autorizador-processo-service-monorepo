import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { errorInterceptor } from '../../../core/http/error.interceptor';
import { environment } from '../../../../environments/environment';
import type { PaginaProcedimentos } from '../../../shared/api';
import { ProcedimentosService } from './procedimentos.service';

const URL = `${environment.apiBaseUrl.replace(/\/+$/, '')}/procedimentos`;

const pagina = (n: number): PaginaProcedimentos => ({
  itens: Array.from({ length: n }, (_, i) => ({
    id: i + 1,
    numProcedimento: 100 + i,
    sexos: ['M', 'F'] as ('M' | 'F')[],
    idades: [18, 19],
    criadoEm: '2026-01-01T00:00:00Z',
  })),
  pagina: 0,
  tamanho: 20,
  totalItens: n,
  totalPaginas: n === 0 ? 0 : 1,
});

describe('ProcedimentosService', () => {
  let service: ProcedimentosService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(ProcedimentosService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('carrega itens com sucesso', () => {
    service.carregar();
    expect(service.carregando()).toBe(true);
    const req = http.expectOne((r) => r.url === URL);
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.has('q')).toBe(false);
    req.flush(pagina(3));
    expect(service.itens().length).toBe(3);
    expect(service.totalItens()).toBe(3);
    expect(service.carregando()).toBe(false);
    expect(service.erro()).toBeNull();
    expect(service.vazio()).toBe(false);
  });

  it('envia a busca q aparada', () => {
    service.carregar({ q: ' 99 ' });
    const req = http.expectOne((r) => r.url === URL);
    expect(req.request.params.get('q')).toBe('99');
    req.flush(pagina(1));
  });

  it('indica lista vazia', () => {
    service.carregar();
    http.expectOne((r) => r.url === URL).flush(pagina(0));
    expect(service.itens()).toEqual([]);
    expect(service.vazio()).toBe(true);
  });

  it('expõe o erro do servidor', () => {
    service.carregar();
    http
      .expectOne((r) => r.url === URL)
      .flush(
        { title: 'Erro', status: 500, code: 'ERRO_INTERNO' },
        { status: 500, statusText: 'x' },
      );
    expect(service.erro()?.kind).toBe('servidor');
    expect(service.carregando()).toBe(false);
    expect(service.vazio()).toBe(false);
  });

  it('cadastra um procedimento via POST', () => {
    let id: number | undefined;
    service
      .cadastrarProcedimento({
        numProcedimento: 9999,
        nome: 'X',
        categoria: 'CONSULTAS',
        sexos: ['M'],
        idades: [1],
      })
      .subscribe((p) => (id = p.id));
    const req = http.expectOne(URL);
    expect(req.request.method).toBe('POST');
    req.flush({ ...pagina(1).itens[0], id: 7 });
    expect(id).toBe(7);
  });
});
