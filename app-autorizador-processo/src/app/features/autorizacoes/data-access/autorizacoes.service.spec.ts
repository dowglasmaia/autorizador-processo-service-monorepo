import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { errorInterceptor } from '../../../core/http/error.interceptor';
import { environment } from '../../../../environments/environment';
import type { PaginaAutorizacoes } from '../../../shared/api';
import { AutorizacoesService } from './autorizacoes.service';

const URL = `${environment.apiBaseUrl.replace(/\/+$/, '')}/autorizacoes`;

const pagina = (n: number): PaginaAutorizacoes => ({
  itens: Array.from({ length: n }, (_, i) => ({
    id: n - i,
    procedimento: {
      id: 1,
      numProcedimento: 4567,
      sexos: ['M', 'F'],
      idades: [20, 30],
      criadoEm: '2026-01-01T00:00:00Z',
    },
    permitido: true,
    criadaEm: '2026-01-02T10:00:00Z',
  })),
  pagina: 0,
  tamanho: 20,
  totalItens: n,
  totalPaginas: n === 0 ? 0 : 1,
});

describe('AutorizacoesService', () => {
  let service: AutorizacoesService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(AutorizacoesService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('carrega a página com sucesso', () => {
    service.listarAutorizacoes(0);
    expect(service.carregando()).toBe(true);
    const req = http.expectOne((r) => r.url === URL);
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('20');
    req.flush(pagina(2));
    expect(service.itens().length).toBe(2);
    expect(service.totalItens()).toBe(2);
    expect(service.totalPaginas()).toBe(1);
    expect(service.carregando()).toBe(false);
    expect(service.erro()).toBeNull();
  });

  it('aceita lista vazia', () => {
    service.listarAutorizacoes(0);
    http.expectOne((r) => r.url === URL).flush(pagina(0));
    expect(service.itens()).toEqual([]);
    expect(service.erro()).toBeNull();
  });

  it('expõe erro de rede', () => {
    service.listarAutorizacoes(0);
    http.expectOne((r) => r.url === URL).error(new ProgressEvent('error'));
    expect(service.erro()).not.toBeNull();
    expect(service.itens()).toEqual([]);
    expect(service.carregando()).toBe(false);
  });
});
