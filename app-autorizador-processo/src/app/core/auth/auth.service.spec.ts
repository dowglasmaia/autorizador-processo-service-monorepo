import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { errorInterceptor } from '../http/error.interceptor';
import type { AppError } from '../errors/app-error';

import { environment } from '../../../environments/environment';
const API = environment.apiBaseUrl;
const CHAVE = 'autorizador.sessao';
const usuario = { id: 1, email: 'ana@exemplo.com' };

function jwt(exp: number): string {
  return `h.${btoa(JSON.stringify({ exp }))}.s`;
}

function montar(): { auth: AuthService; ctrl: HttpTestingController; router: Router } {
  TestBed.resetTestingModule();
  TestBed.configureTestingModule({
    providers: [
      provideZonelessChangeDetection(),
      provideRouter([]),
      provideHttpClient(withInterceptors([errorInterceptor])),
      provideHttpClientTesting(),
    ],
  });
  return {
    auth: TestBed.inject(AuthService),
    ctrl: TestBed.inject(HttpTestingController),
    router: TestBed.inject(Router),
  };
}

describe('AuthService', () => {
  beforeEach(() => {
    sessionStorage.clear();
    vi.useFakeTimers();
  });
  afterEach(() => {
    vi.useRealTimers();
    sessionStorage.clear();
  });

  it('entrar com sucesso guarda sessão em memória e no sessionStorage', () => {
    const { auth, ctrl } = montar();
    const exp = Math.floor(Date.now() / 1000) + 3600;
    auth.entrar({ email: 'ana@exemplo.com', senha: 'x' }).subscribe();
    const req = ctrl.expectOne(`${API}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ accessToken: jwt(exp), tokenType: 'Bearer', expiraEm: 3600, usuario });
    expect(auth.autenticado()).toBe(true);
    expect(auth.usuario()).toEqual(usuario);
    expect(auth.expiraEm()).toBe(exp * 1000);
    expect(sessionStorage.getItem(CHAVE)).toContain('ana@exemplo.com');
  });

  it('sem exp legível no JWT usa expiraEm da resposta', () => {
    const { auth, ctrl } = montar();
    auth.entrar({ email: 'a@b.c', senha: 'x' }).subscribe();
    ctrl
      .expectOne(`${API}/auth/login`)
      .flush({ accessToken: 'opaco', tokenType: 'Bearer', expiraEm: 60, usuario });
    expect(auth.expiraEm()).toBe(Date.now() + 60_000);
  });

  it('401 propaga AppError CREDENCIAIS_INVALIDAS e não cria sessão', () => {
    const { auth, ctrl } = montar();
    let erro: AppError | undefined;
    auth.entrar({ email: 'a@b.c', senha: 'x' }).subscribe({ error: (e: AppError) => (erro = e) });
    ctrl
      .expectOne(`${API}/auth/login`)
      .flush(
        { code: 'CREDENCIAIS_INVALIDAS', detail: 'Inválidas' },
        { status: 401, statusText: '' },
      );
    expect(erro?.code).toBe('CREDENCIAIS_INVALIDAS');
    expect(auth.autenticado()).toBe(false);
    expect(sessionStorage.getItem(CHAVE)).toBeNull();
  });

  it('429 traz retryAfter de Retry-After', () => {
    const { auth, ctrl } = montar();
    let erro: AppError | undefined;
    auth.entrar({ email: 'a@b.c', senha: 'x' }).subscribe({ error: (e: AppError) => (erro = e) });
    ctrl
      .expectOne(`${API}/auth/login`)
      .flush(
        { code: 'MUITAS_TENTATIVAS', detail: 'Calma' },
        { status: 429, statusText: '', headers: { 'Retry-After': '30' } },
      );
    expect(erro?.retryAfter).toBe(30);
    expect(auth.autenticado()).toBe(false);
  });

  it('falha de rede vira AppError do tipo rede', () => {
    const { auth, ctrl } = montar();
    let erro: AppError | undefined;
    auth.entrar({ email: 'a@b.c', senha: 'x' }).subscribe({ error: (e: AppError) => (erro = e) });
    ctrl.expectOne(`${API}/auth/login`).error(new ProgressEvent('error'));
    expect(erro?.kind).toBe('rede');
  });

  it('restaura a sessão válida após reload', () => {
    sessionStorage.setItem(
      CHAVE,
      JSON.stringify({ token: 't', usuario, expiraEm: Date.now() + 60_000 }),
    );
    const { auth } = montar();
    expect(auth.autenticado()).toBe(true);
    expect(auth.token()).toBe('t');
    expect(auth.usuario()).toEqual(usuario);
  });

  it('descarta sessão armazenada expirada e sinaliza o aviso', () => {
    sessionStorage.setItem(
      CHAVE,
      JSON.stringify({ token: 't', usuario, expiraEm: Date.now() - 1 }),
    );
    const { auth } = montar();
    expect(auth.autenticado()).toBe(false);
    expect(auth.sessaoExpirada()).toBe(true);
    expect(sessionStorage.getItem(CHAVE)).toBeNull();
  });

  it('ignora conteúdo corrompido no armazenamento', () => {
    sessionStorage.setItem(CHAVE, '{nao-json');
    expect(montar().auth.autenticado()).toBe(false);
  });

  it('agenda o logout no exp: encerra, avisa e navega ao login', () => {
    sessionStorage.setItem(
      CHAVE,
      JSON.stringify({ token: 't', usuario, expiraEm: Date.now() + 5_000 }),
    );
    const { auth, router } = montar();
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    vi.advanceTimersByTime(5_001);
    expect(auth.autenticado()).toBe(false);
    expect(auth.sessaoExpirada()).toBe(true);
    expect(navegar).toHaveBeenCalledWith(['/login'], { queryParams: {} });
  });

  it('sair limpa estado, armazenamento, executa limpezas e preserva retorno', () => {
    sessionStorage.setItem(
      CHAVE,
      JSON.stringify({ token: 't', usuario, expiraEm: Date.now() + 60_000 }),
    );
    const { auth, router } = montar();
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const limpeza = vi.fn();
    auth.registrarLimpeza(limpeza);
    auth.sair({ retorno: '/procedimentos' });
    expect(auth.token()).toBeNull();
    expect(auth.usuario()).toBeNull();
    expect(sessionStorage.getItem(CHAVE)).toBeNull();
    expect(limpeza).toHaveBeenCalledOnce();
    expect(navegar).toHaveBeenCalledWith(['/login'], {
      queryParams: { retorno: '/procedimentos' },
    });
  });
});
