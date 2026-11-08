import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { errorInterceptor } from './error.interceptor';
import { AppError, MENSAGEM_REDE, MENSAGEM_SERVIDOR } from '../errors/app-error';
import type { ProblemCode } from '../../shared/api';

const CODES: Record<ProblemCode, number> = {
  VALIDACAO_INVALIDA: 400,
  CORPO_INVALIDO: 400,
  PARAMETRO_INVALIDO: 400,
  PROCEDIMENTO_DUPLICADO: 403,
  AUTORIZACAO_NEGADA: 403,
  PROCEDIMENTO_NAO_ENCONTRADO: 404,
  ERRO_INTERNO: 500,
  CREDENCIAIS_INVALIDAS: 401,
  NAO_AUTENTICADO: 401,
  SESSAO_EXPIRADA: 401,
  ACESSO_NEGADO: 403,
  MUITAS_TENTATIVAS: 429,
};

describe('errorInterceptor', () => {
  let http: HttpClient;
  let ctrl: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    ctrl = TestBed.inject(HttpTestingController);
  });
  afterEach(() => ctrl.verify());

  /** Dispara um GET, deixa o teste responder e devolve o `AppError` recebido. */
  function falha(responder: (req: ReturnType<typeof ctrl.expectOne>) => void): AppError {
    let erro: AppError | undefined;
    http.get('/x').subscribe({ error: (e: AppError) => (erro = e) });
    responder(ctrl.expectOne('/x'));
    expect(erro).toBeDefined();
    return erro as AppError;
  }

  for (const [code, status] of Object.entries(CODES)) {
    it(`converte ${code} (${status}) em AppError com o mesmo code`, () => {
      const erro = falha((r) =>
        r.flush(
          { code, title: 't', detail: 'd', status },
          { status, statusText: 'x', headers: { 'Content-Type': 'application/problem+json' } },
        ),
      );
      expect(erro.code).toBe(code);
      expect(erro.status).toBe(status);
    });
  }

  it('403 PROCEDIMENTO_DUPLICADO vira kind negocio', () => {
    const erro = falha((r) =>
      r.flush(
        { code: 'PROCEDIMENTO_DUPLICADO', detail: 'Já existe' },
        { status: 403, statusText: 'Forbidden' },
      ),
    );
    expect(erro).toMatchObject({ kind: 'negocio', code: 'PROCEDIMENTO_DUPLICADO' });
  });

  it('rede fora (status 0) vira kind rede', () => {
    const erro = falha((r) => r.error(new ProgressEvent('error'), { status: 0 }));
    expect(erro).toMatchObject({ kind: 'rede', detail: MENSAGEM_REDE });
  });

  it('500 genérico sem problem+json vira servidor sem detalhes técnicos', () => {
    const erro = falha((r) =>
      r.flush('<html>stacktrace</html>', { status: 500, statusText: 'Server Error' }),
    );
    expect(erro).toMatchObject({ kind: 'servidor', detail: MENSAGEM_SERVIDOR });
    expect(erro.code).toBeUndefined();
  });

  it('JSON inválido (erro de parsing em resposta 200) vira servidor', () => {
    const erro = falha((r) =>
      r.error(new ProgressEvent('error'), { status: 200, statusText: 'Parsing error' }),
    );
    expect(erro).toMatchObject({ kind: 'servidor', detail: MENSAGEM_SERVIDOR });
  });

  it('errors[] de validação vira campos', () => {
    const erro = falha((r) =>
      r.flush(
        { code: 'VALIDACAO_INVALIDA', errors: [{ campo: 'codigo', mensagem: 'obrigatório' }] },
        { status: 400, statusText: 'Bad Request' },
      ),
    );
    expect(erro.kind).toBe('validacao');
    expect(erro.campos).toEqual({ codigo: 'obrigatório' });
  });
});
