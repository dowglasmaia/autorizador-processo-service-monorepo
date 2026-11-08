import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { errorInterceptor } from '../../../core/http/error.interceptor';
import { environment } from '../../../../environments/environment';
import type { AppError } from '../../../core/errors/app-error';
import { AutorizacaoService, type ResultadoSolicitacao } from './autorizacao.service';

const BASE = environment.apiBaseUrl.replace(/\/+$/, '');
const corpo = { numProcedimento: 4567, idade: 20, sexo: 'F' };
const procedimento = {
  id: 1,
  numProcedimento: 4567,
  sexos: ['F' as const],
  idades: [20],
  criadoEm: '2026-01-01T00:00:00Z',
};

describe('AutorizacaoService', () => {
  let service: AutorizacaoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(AutorizacaoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function solicitar(): { resultado?: ResultadoSolicitacao; erro?: AppError } {
    const saida: { resultado?: ResultadoSolicitacao; erro?: AppError } = {};
    service.solicitarAutorizacao(corpo).subscribe({
      next: (r) => (saida.resultado = r),
      error: (e: AppError) => (saida.erro = e),
    });
    return saida;
  }

  it('201 devolve autorizado com a autorização', () => {
    const saida = solicitar();
    const req = http.expectOne(`${BASE}/autorizacoes`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(corpo);
    const autorizacao = { id: 9, procedimento, permitido: true, criadaEm: null };
    req.flush(autorizacao, { status: 201, statusText: 'Created' });
    expect(saida.resultado).toEqual({ resultado: 'autorizado', autorizacao });
  });

  it('403 AUTORIZACAO_NEGADA devolve negado com a mensagem', () => {
    const saida = solicitar();
    http
      .expectOne(`${BASE}/autorizacoes`)
      .flush(
        { title: 'Negada', status: 403, code: 'AUTORIZACAO_NEGADA', detail: 'Autorização negada.' },
        { status: 403, statusText: 'Forbidden' },
      );
    expect(saida.resultado).toEqual({ resultado: 'negado', detail: 'Autorização negada.' });
    expect(saida.erro).toBeUndefined();
  });

  it('404 propaga PROCEDIMENTO_NAO_ENCONTRADO', () => {
    const saida = solicitar();
    http
      .expectOne(`${BASE}/autorizacoes`)
      .flush(
        { title: 'x', status: 404, code: 'PROCEDIMENTO_NAO_ENCONTRADO', detail: 'Não existe.' },
        { status: 404, statusText: 'Not Found' },
      );
    expect(saida.erro?.kind).toBe('nao-encontrado');
    expect(saida.erro?.code).toBe('PROCEDIMENTO_NAO_ENCONTRADO');
  });

  it('400 propaga VALIDACAO_INVALIDA com campos', () => {
    const saida = solicitar();
    http.expectOne(`${BASE}/autorizacoes`).flush(
      {
        title: 'x',
        status: 400,
        code: 'VALIDACAO_INVALIDA',
        detail: 'Inválido.',
        errors: [{ campo: 'idade', mensagem: 'Idade inválida.' }],
      },
      { status: 400, statusText: 'Bad Request' },
    );
    expect(saida.erro?.kind).toBe('validacao');
    expect(saida.erro?.campos).toEqual({ idade: 'Idade inválida.' });
  });

  it('falha de rede propaga erro do tipo rede', () => {
    const saida = solicitar();
    http.expectOne(`${BASE}/autorizacoes`).error(new ProgressEvent('error'));
    expect(saida.erro?.kind).toBe('rede');
  });

  it('lista o seletor com size=200 e ordenação, devolvendo só os itens', () => {
    let itens: readonly unknown[] = [];
    service.listarProcedimentos().subscribe((i) => (itens = i));
    const req = http.expectOne((r) => r.url === `${BASE}/procedimentos`);
    expect(req.request.params.get('size')).toBe('200');
    expect(req.request.params.get('sort')).toBe('numProcedimento,asc');
    req.flush({ itens: [procedimento], pagina: 0, tamanho: 200, totalItens: 1, totalPaginas: 1 });
    expect(itens).toEqual([procedimento]);
  });
});
