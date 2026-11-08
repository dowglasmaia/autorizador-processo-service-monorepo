import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { routes } from '../../app.routes';
import { App } from '../../app';

/**
 * O ambiente de teste (jsdom) não aplica Tailwind; por isso a responsividade em 320/768/1280 px é
 * verificada pelas classes de breakpoint (mobile-first) e pelo comportamento da gaveta.
 */
describe('Shell', () => {
  afterEach(() => sessionStorage.clear());

  let fixture: ReturnType<typeof TestBed.createComponent<App>>;
  let el: HTMLElement;

  beforeEach(async () => {
    sessionStorage.setItem(
      'autorizador.sessao',
      JSON.stringify({
        token: 't',
        usuario: { id: 1, email: 'ana@exemplo.com' },
        expiraEm: Date.now() + 3_600_000,
      }),
    );
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideRouter(routes),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    fixture = TestBed.createComponent(App);
    el = fixture.nativeElement;
    await TestBed.inject(Router).navigateByUrl('/painel');
    await fixture.whenStable();
    TestBed.inject(HttpTestingController).expectOne('/actuator/health').flush({ status: 'UP' });
    await fixture.whenStable();
  });

  it('tem landmarks: nav principal, main e link de pular conteúdo', () => {
    expect(el.querySelector('nav[aria-label="Principal"]')).not.toBeNull();
    expect(el.querySelector('main#conteudo')).not.toBeNull();
    expect(el.querySelector('a[href="#conteudo"]')).not.toBeNull();
    expect(el.querySelectorAll('nav a').length).toBe(4);
  });

  it('320 px: barra superior visível (lg:hidden) e gaveta fora da tela por padrão', () => {
    expect(el.querySelector('header')!.className).toContain('lg:hidden');
    const aside = el.querySelector('aside')!;
    expect(aside.className).toContain('-translate-x-full');
    expect(aside.className).toContain('lg:translate-x-0');
  });

  it('768 px: ainda usa a gaveta (sidebar só a partir de lg); botão do menu tem alvo de 44 px', () => {
    const botao = el.querySelector('header button')!;
    expect(botao.className).toContain('size-11');
    expect(botao.getAttribute('aria-expanded')).toBe('false');
  });

  it('1280 px: conteúdo reserva a largura da sidebar (lg:ml-64)', () => {
    expect(el.querySelector('main')!.className).toContain('lg:ml-64');
  });

  it('teclado: abrir a gaveta e fechar com Esc', async () => {
    const botao = el.querySelector<HTMLButtonElement>('header button')!;
    botao.click();
    await fixture.whenStable();
    expect(botao.getAttribute('aria-expanded')).toBe('true');
    expect(el.querySelector('aside')!.className).toContain('translate-x-0');
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    await fixture.whenStable();
    expect(botao.getAttribute('aria-expanded')).toBe('false');
  });

  it('a raiz redireciona para /painel, marca item ativo e mostra status da API', async () => {
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/');
    await fixture.whenStable();
    expect(router.url).toBe('/painel');
    expect(el.querySelector('nav a[aria-current="page"]')!.textContent).toContain('Painel');
    expect(el.querySelector('aside [role="status"]')!.textContent).toContain('disponível');
  });

  it('rota desconhecida mostra "Página não encontrada"', async () => {
    await TestBed.inject(Router).navigateByUrl('/inexistente');
    await fixture.whenStable();
    expect(el.textContent).toContain('Página não encontrada');
  });

  it('galeria /_ds existe em desenvolvimento', () => {
    expect(routes.some((r) => r.children?.some((c) => c.path === '_ds'))).toBe(true);
  });
});
