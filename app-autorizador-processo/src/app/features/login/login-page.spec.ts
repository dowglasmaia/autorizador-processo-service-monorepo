import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { routes } from '../../app.routes';
import { App } from '../../app';
import { errorInterceptor } from '../../core/http/error.interceptor';
import { authInterceptor } from '../../core/auth/auth.interceptor';
import { AuthService } from '../../core/auth/auth.service';
import { ErrorDialogService } from '../../core/feedback/error-dialog';
import { classificarErroLogin, mensagemMuitasTentativas } from './mensagens-login';
import { environment } from '../../../environments/environment';

const API = environment.apiBaseUrl;

describe('mensagens de login', () => {
  it('classifica por code', () => {
    expect(
      classificarErroLogin({ kind: 'negocio', code: 'CREDENCIAIS_INVALIDAS', detail: 'x' }),
    ).toEqual({ tipo: 'alerta', mensagem: 'E-mail ou senha incorretos.' });
    expect(
      classificarErroLogin({
        kind: 'negocio',
        code: 'MUITAS_TENTATIVAS',
        detail: 'x',
        retryAfter: 90,
      }),
    ).toEqual({ tipo: 'alerta', mensagem: 'Muitas tentativas. Tente novamente em 2 minutos.' });
    expect(
      classificarErroLogin({ kind: 'validacao', detail: 'x', campos: { email: 'inválido' } }),
    ).toEqual({
      tipo: 'campos',
      campos: { email: 'inválido' },
    });
    expect(classificarErroLogin({ kind: 'rede', detail: 'sem rede' })).toEqual({
      tipo: 'modal',
      mensagem: 'sem rede',
    });
  });

  it('formata espera singular/plural e ausente', () => {
    expect(mensagemMuitasTentativas(1)).toContain('1 segundo.');
    expect(mensagemMuitasTentativas(30)).toContain('30 segundos');
    expect(mensagemMuitasTentativas()).toContain('Aguarde');
  });
});

describe('Login e menu do usuário', () => {
  let fixture: ReturnType<typeof TestBed.createComponent<App>>;
  let el: HTMLElement;
  let router: Router;
  let ctrl: HttpTestingController;

  function montar(): void {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideRouter(routes),
        provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    router = TestBed.inject(Router);
    ctrl = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(App);
    el = fixture.nativeElement;
  }

  async function irA(url: string): Promise<void> {
    await router.navigateByUrl(url);
    await fixture.whenStable();
  }

  function preencherEEnviar(email = 'ana@exemplo.com', senha = 'segredo'): void {
    const e = el.querySelector<HTMLInputElement>('input[type="email"]')!;
    const s = el.querySelector<HTMLInputElement>('input[autocomplete="current-password"]')!;
    e.value = email;
    e.dispatchEvent(new Event('input'));
    s.value = senha;
    s.dispatchEvent(new Event('input'));
    el.querySelector<HTMLFormElement>('form')!.dispatchEvent(new Event('submit'));
  }

  beforeEach(() => sessionStorage.clear());
  afterEach(() => sessionStorage.clear());

  describe('página de login', () => {
    beforeEach(() => montar());

    it('anônimo em rota protegida vai ao login com retorno; tem rótulos, h1 e autocomplete', async () => {
      await irA('/procedimentos');
      expect(router.url).toBe('/login?retorno=%2Fprocedimentos');
      expect(el.querySelector('h1')!.textContent).toContain('Entrar');
      expect(el.querySelectorAll('label').length).toBe(2);
      expect(el.querySelector('input[autocomplete="username"]')).not.toBeNull();
      expect(el.querySelector('input[autocomplete="current-password"]')).not.toBeNull();
    });

    it('foco inicial no e-mail', async () => {
      await irA('/login');
      expect(document.activeElement).toBe(el.querySelector('input[type="email"]'));
    });

    it('mostrar/ocultar senha com aria-pressed', async () => {
      await irA('/login');
      const botao = el.querySelector<HTMLButtonElement>('button[aria-pressed]')!;
      const senha = el.querySelector<HTMLInputElement>('input[autocomplete="current-password"]')!;
      expect(botao.getAttribute('aria-pressed')).toBe('false');
      expect(senha.type).toBe('password');
      botao.click();
      await fixture.whenStable();
      expect(botao.getAttribute('aria-pressed')).toBe('true');
      expect(senha.type).toBe('text');
    });

    it('campos vazios mostram erros e não chamam a API', async () => {
      await irA('/login');
      el.querySelector<HTMLFormElement>('form')!.dispatchEvent(new Event('submit'));
      await fixture.whenStable();
      expect(el.textContent).toContain('Informe o e-mail.');
      expect(el.textContent).toContain('Informe a senha.');
      ctrl.expectNone(`${API}/auth/login`);
    });

    it('sucesso entra e segue o retorno seguro; retorno malicioso cai no painel', async () => {
      await irA('/login?retorno=%2F%2Fevil.com');
      preencherEEnviar();
      ctrl.expectOne(`${API}/auth/login`).flush({
        accessToken: 'opaco',
        tokenType: 'Bearer',
        expiraEm: 3600,
        usuario: { id: 1, email: 'ana@exemplo.com' },
      });
      await fixture.whenStable();
      expect(router.url).toBe('/painel');
      expect(TestBed.inject(AuthService).autenticado()).toBe(true);
      ctrl.match('/actuator/health').forEach((r) => r.flush({ status: 'UP' }));
    });

    it('401 mostra alerta genérico', async () => {
      await irA('/login');
      preencherEEnviar();
      ctrl
        .expectOne(`${API}/auth/login`)
        .flush({ code: 'CREDENCIAIS_INVALIDAS', detail: 'x' }, { status: 401, statusText: '' });
      await fixture.whenStable();
      expect(el.querySelector('[role="alert"]')!.textContent).toContain(
        'E-mail ou senha incorretos.',
      );
      expect(router.url).toBe('/login');
    });

    it('429 mostra o tempo de espera', async () => {
      await irA('/login');
      preencherEEnviar();
      ctrl
        .expectOne(`${API}/auth/login`)
        .flush(
          { code: 'MUITAS_TENTATIVAS', detail: 'x' },
          { status: 429, statusText: '', headers: { 'Retry-After': '45' } },
        );
      await fixture.whenStable();
      expect(el.querySelector('[role="alert"]')!.textContent).toContain('45 segundos');
    });

    it('erro de rede abre o modal com nova tentativa', async () => {
      await irA('/login');
      preencherEEnviar();
      ctrl.expectOne(`${API}/auth/login`).error(new ProgressEvent('error'));
      await fixture.whenStable();
      expect(TestBed.inject(ErrorDialogService).state()?.retry).toBeDefined();
      TestBed.inject(ErrorDialogService).retry();
      ctrl.expectOne(`${API}/auth/login`);
    });

    it('sessão expirada mostra o aviso', async () => {
      sessionStorage.setItem(
        'autorizador.sessao',
        JSON.stringify({
          token: 't',
          usuario: { id: 1, email: 'a@b.c' },
          expiraEm: Date.now() - 1000,
        }),
      );
      TestBed.resetTestingModule();
      montar();
      await irA('/login');
      expect(el.querySelector('[role="status"]')!.textContent).toContain('Sua sessão expirou');
    });

    it('autenticado em /login é enviado ao painel', async () => {
      sessionStorage.setItem(
        'autorizador.sessao',
        JSON.stringify({
          token: 't',
          usuario: { id: 1, email: 'a@b.c' },
          expiraEm: Date.now() + 60_000,
        }),
      );
      TestBed.resetTestingModule();
      montar();
      await irA('/login');
      expect(router.url).toBe('/painel');
      ctrl.match('/actuator/health').forEach((r) => r.flush({ status: 'UP' }));
    });
  });

  describe('menu do usuário no shell', () => {
    beforeEach(async () => {
      sessionStorage.setItem(
        'autorizador.sessao',
        JSON.stringify({
          token: 't',
          usuario: { id: 1, email: 'ana@exemplo.com' },
          expiraEm: Date.now() + 3_600_000,
        }),
      );
      montar();
      await irA('/painel');
      ctrl.match('/actuator/health').forEach((r) => r.flush({ status: 'UP' }));
      await fixture.whenStable();
    });

    it('abre por botão com aria-expanded, mostra e-mail e Sair; Esc fecha', async () => {
      const gatilho = el.querySelector<HTMLButtonElement>('button[aria-controls="menu-usuario"]')!;
      expect(gatilho.getAttribute('aria-expanded')).toBe('false');
      expect(el.querySelector('#menu-usuario')).toBeNull();
      gatilho.click();
      await fixture.whenStable();
      expect(gatilho.getAttribute('aria-expanded')).toBe('true');
      expect(el.querySelector('#menu-usuario')!.textContent).toContain('ana@exemplo.com');
      document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
      await fixture.whenStable();
      expect(el.querySelector('#menu-usuario')).toBeNull();
    });

    it('Sair encerra a sessão e navega ao login', async () => {
      el.querySelector<HTMLButtonElement>('button[aria-controls="menu-usuario"]')!.click();
      await fixture.whenStable();
      const sair = [...el.querySelectorAll<HTMLButtonElement>('#menu-usuario button')].find((b) =>
        b.textContent?.includes('Sair'),
      )!;
      sair.click();
      await fixture.whenStable();
      expect(TestBed.inject(AuthService).autenticado()).toBe(false);
      expect(sessionStorage.getItem('autorizador.sessao')).toBeNull();
      expect(router.url).toBe('/login');
    });
  });
});
