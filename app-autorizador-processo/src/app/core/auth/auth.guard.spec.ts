import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors, HttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import { authGuard, guestGuard } from './auth.guard';
import { authInterceptor, urlDaApi } from './auth.interceptor';
import { AuthService } from './auth.service';
import { retornoSeguro } from './retorno-seguro';
import { errorInterceptor } from '../http/error.interceptor';

import { environment } from '../../../environments/environment';
const API = environment.apiBaseUrl;
const CHAVE = 'autorizador.sessao';

function sessao(): void {
  sessionStorage.setItem(
    CHAVE,
    JSON.stringify({
      token: 'tok',
      usuario: { id: 1, email: 'ana@exemplo.com' },
      expiraEm: Date.now() + 3_600_000,
    }),
  );
}

function config(): void {
  TestBed.configureTestingModule({
    providers: [
      provideZonelessChangeDetection(),
      provideRouter([
        { path: 'login', children: [] },
        { path: 'painel', children: [] },
      ]),
      provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
      provideHttpClientTesting(),
    ],
  });
}

describe('retornoSeguro', () => {
  it.each([
    ['/procedimentos', '/procedimentos'],
    ['/procedimentos?pagina=2', '/procedimentos?pagina=2'],
    [null, '/painel'],
    [undefined, '/painel'],
    ['', '/painel'],
    ['//evil.com', '/painel'],
    ['http://evil.com', '/painel'],
    ['javascript:alert(1)', '/painel'],
    ['/\\evil.com', '/painel'],
    ['/a\nb', '/painel'],
    ['/login', '/painel'],
    ['/login?retorno=/x', '/painel'],
    ['relativo', '/painel'],
  ])('%s -> %s', (entrada, esperado) => {
    expect(retornoSeguro(entrada)).toBe(esperado);
  });
});

describe('guards', () => {
  beforeEach(() => sessionStorage.clear());
  afterEach(() => sessionStorage.clear());

  it('authGuard libera com sessão', () => {
    sessao();
    config();
    expect(TestBed.runInInjectionContext(() => authGuard({} as never, []))).toBe(true);
  });

  it('authGuard sem sessão redireciona a /login preservando o destino', () => {
    config();
    const r = TestBed.runInInjectionContext(() =>
      authGuard({} as never, [{ path: 'procedimentos' } as never]),
    ) as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(r)).toBe('/login?retorno=%2Fprocedimentos');
  });

  it('guestGuard libera anônimo e manda autenticado ao painel', () => {
    config();
    expect(TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never))).toBe(true);
    sessao();
    TestBed.resetTestingModule();
    config();
    const r = TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never)) as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(r)).toBe('/painel');
  });
});

describe('authInterceptor', () => {
  let http: HttpClient;
  let ctrl: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    sessao();
    config();
    http = TestBed.inject(HttpClient);
    ctrl = TestBed.inject(HttpTestingController);
  });
  afterEach(() => {
    ctrl.verify();
    sessionStorage.clear();
  });

  it('anexa Bearer às URLs da API', () => {
    http.get(`${API}/procedimentos`).subscribe();
    const req = ctrl.expectOne(`${API}/procedimentos`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer tok');
    req.flush({});
  });

  it('não anexa Bearer a host externo nem a //host', () => {
    http.get('https://externo.com/api/v1/x').subscribe();
    const externo = ctrl.expectOne('https://externo.com/api/v1/x');
    expect(externo.request.headers.has('Authorization')).toBe(false);
    externo.flush({});
    http.get('//externo.com/api/v1/x').subscribe();
    const semEsquema = ctrl.expectOne('//externo.com/api/v1/x');
    expect(semEsquema.request.headers.has('Authorization')).toBe(false);
    semEsquema.flush({});
  });

  it('401 da API encerra a sessão e navega ao login com aviso', () => {
    const auth = TestBed.inject(AuthService);
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    http.get(`${API}/painel`).subscribe({ error: () => undefined });
    ctrl
      .expectOne(`${API}/painel`)
      .flush({ code: 'SESSAO_EXPIRADA', detail: 'x' }, { status: 401, statusText: '' });
    expect(auth.autenticado()).toBe(false);
    expect(auth.sessaoExpirada()).toBe(true);
    expect(navegar).toHaveBeenCalled();
    expect(sessionStorage.getItem(CHAVE)).toBeNull();
  });

  it('401 de CREDENCIAIS_INVALIDAS não derruba a sessão', () => {
    const auth = TestBed.inject(AuthService);
    http.get(`${API}/x`).subscribe({ error: () => undefined });
    ctrl
      .expectOne(`${API}/x`)
      .flush({ code: 'CREDENCIAIS_INVALIDAS', detail: 'x' }, { status: 401, statusText: '' });
    expect(auth.autenticado()).toBe(true);
  });

  it('urlDaApi aceita base relativa e absoluta', () => {
    expect(urlDaApi('/api/v1/x', '/api/v1')).toBe(true);
    expect(urlDaApi('/api/v10/x', '/api/v1')).toBe(false);
    expect(urlDaApi('http://h/api/x', 'http://h/api')).toBe(true);
    expect(urlDaApi('http://outro/api/x', 'http://h/api')).toBe(false);
  });
});
