// @ts-expect-error -- sem @types/node no projeto; o módulo existe no runtime de testes
import { readFileSync } from 'node:fs';
import {
  ChangeDetectionStrategy,
  Component,
  Type,
  provideZonelessChangeDetection,
  signal,
} from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { errorInterceptor } from './core/http/error.interceptor';
import { Dialog } from './shared/ui';
import { LoginPage } from './features/login/pages/login-page';
import { PainelPage } from './features/painel/pages/painel-page';
import { ProcedimentosPage } from './features/procedimentos/pages/procedimentos-page';
import { ProcedimentoNovoPage } from './features/procedimentos/pages/procedimento-novo-page';
import { SolicitarPage } from './features/solicitar/pages/solicitar-page';
import { AutorizacoesPage } from './features/autorizacoes/pages/autorizacoes-page';

/**
 * Auditoria automatizada de acessibilidade (WCAG 2.2 AA) sem dependência externa (sem axe-core):
 * nomes acessíveis, rótulos, ids únicos, foco, alvos de toque, `lang`, títulos e contraste.
 * Violações não cobertas por regra automática estão registradas em `doc.md` (seção Acessibilidade).
 */

const pagina = { itens: [], pagina: 0, tamanho: 20, totalItens: 0, totalPaginas: 0 };
const resumo = {
  totalProcedimentos: 3,
  totalAutorizacoes: 1,
  idadesCobertas: 3,
  coberturaPorIdade: [{ idade: 10, quantidade: 2 }],
  distribuicaoSexos: { masculino: 2, feminino: 3 },
  ultimasAutorizacoes: [],
};

interface Rota {
  nome: string;
  tipo: Type<unknown>;
  resposta?: object;
}

const ROTAS: readonly Rota[] = [
  { nome: '/login', tipo: LoginPage },
  { nome: '/painel', tipo: PainelPage, resposta: resumo },
  { nome: '/procedimentos', tipo: ProcedimentosPage, resposta: pagina },
  { nome: '/procedimentos/novo', tipo: ProcedimentoNovoPage },
  { nome: '/solicitar', tipo: SolicitarPage, resposta: pagina },
  { nome: '/autorizacoes', tipo: AutorizacoesPage, resposta: pagina },
];

function nomeAcessivel(el: Element, raiz: Element): string {
  const aria = el.getAttribute('aria-label')?.trim();
  if (aria) return aria;
  const por = el.getAttribute('aria-labelledby');
  if (por) {
    const t = por
      .split(/\s+/)
      .map((id) => raiz.querySelector(`[id="${id}"]`)?.textContent ?? '')
      .join(' ')
      .trim();
    if (t) return t;
  }
  const id = el.getAttribute('id');
  if (id) {
    const rotulo = raiz.querySelector(`label[for="${id}"]`)?.textContent?.trim();
    if (rotulo) return rotulo;
  }
  const envolvente = el.closest('label')?.textContent?.trim();
  if (envolvente) return envolvente;
  return (el.textContent ?? '').trim() || el.getAttribute('title')?.trim() || '';
}

function violacoes(raiz: HTMLElement): string[] {
  const v: string[] = [];
  raiz.querySelectorAll('button, a[href], [routerlink]').forEach((el) => {
    if (!nomeAcessivel(el, raiz)) v.push(`sem nome acessível: <${el.tagName.toLowerCase()}>`);
  });
  raiz.querySelectorAll('input:not([type=hidden]), select, textarea').forEach((el) => {
    if (!nomeAcessivel(el, raiz)) v.push(`campo sem rótulo: <${el.tagName.toLowerCase()}>`);
  });
  raiz.querySelectorAll('[role=img]').forEach((el) => {
    if (!el.getAttribute('aria-label')) v.push('role=img sem aria-label');
  });
  raiz.querySelectorAll('img').forEach((el) => {
    if (!el.hasAttribute('alt')) v.push('img sem alt');
  });
  const ids = Array.from(raiz.querySelectorAll('[id]')).map((e) => e.id);
  ids.filter((id, i) => ids.indexOf(id) !== i).forEach((id) => v.push(`id duplicado: ${id}`));
  raiz.querySelectorAll('[tabindex]').forEach((el) => {
    if (Number(el.getAttribute('tabindex')) > 0) v.push('tabindex positivo');
  });
  raiz.querySelectorAll('[aria-describedby]').forEach((el) => {
    for (const id of (el.getAttribute('aria-describedby') ?? '').split(/\s+/).filter(Boolean)) {
      if (!raiz.querySelector(`[id="${id}"]`))
        v.push(`aria-describedby aponta para ${id} inexistente`);
    }
  });
  raiz.querySelectorAll('table').forEach((t) => {
    if (!t.querySelector('caption')?.textContent?.trim()) v.push('tabela sem caption');
  });
  const titulos = Array.from(raiz.querySelectorAll('h1, h2, h3, h4')).map((h) =>
    Number(h.tagName[1]),
  );
  titulos.forEach((n, i) => {
    if (i > 0 && n - titulos[i - 1] > 1)
      v.push(`salto de nível de título h${titulos[i - 1]}→h${n}`);
  });
  return v;
}

/** Alvos de toque: todo botão/link de ação precisa declarar altura mínima de 44 px (min-h-11 / size-11). */
function alvosPequenos(raiz: HTMLElement): string[] {
  return Array.from(raiz.querySelectorAll('button, a[href]'))
    .filter((el) => !/\b(min-h-11|size-11)\b/.test(el.className))
    .map((el) => `${el.tagName.toLowerCase()} "${(el.textContent ?? '').trim().slice(0, 30)}"`);
}

describe('a11y — 6 rotas', () => {
  for (const rota of ROTAS) {
    it(`${rota.nome}: sem violações automáticas e alvos de toque ≥ 44 px`, async () => {
      TestBed.configureTestingModule({
        providers: [
          provideZonelessChangeDetection(),
          provideRouter([]),
          provideHttpClient(withInterceptors([errorInterceptor])),
          provideHttpClientTesting(),
        ],
      });
      const http = TestBed.inject(HttpTestingController);
      const fixture = TestBed.createComponent(rota.tipo);
      fixture.detectChanges();
      const el = fixture.nativeElement as HTMLElement;
      const corpo = rota.resposta;
      if (corpo) {
        http.match(() => true).forEach((r) => r.flush(corpo));
        await fixture.whenStable();
        fixture.detectChanges();
      }
      expect(violacoes(el)).toEqual([]);
      expect(alvosPequenos(el)).toEqual([]);
      TestBed.resetTestingModule();
    });
  }
});

describe('a11y — documento, rotas e teclado', () => {
  it('index.html declara lang pt-BR', () => {
    expect(readFileSync('src/index.html', 'utf8')).toMatch(/<html[^>]*\slang="pt-BR"/);
  });

  it('toda rota de página define título "… · Autorizador"', () => {
    for (const f of ['autorizacoes', 'login', 'painel', 'procedimentos', 'solicitar']) {
      const src: string = readFileSync(`src/app/features/${f}/${f}.routes.ts`, 'utf8');
      const titulos = [...src.matchAll(/title:\s*'([^']+)'/g)].map((m) => m[1]);
      expect(titulos.length).toBeGreaterThan(0);
      titulos.forEach((t) => expect(t).toMatch(/ · Autorizador$/));
    }
  });

  it('nenhum (click) em elemento não interativo (teclado sempre alcança a ação)', () => {
    const arquivos = [
      'layout/shell/shell.ts',
      'features/procedimentos/pages/procedimento-novo-page.ts',
      'features/solicitar/pages/solicitar-page.ts',
      'shared/ui/chip/chip.ts',
    ];
    for (const a of arquivos) {
      const src: string = readFileSync(`src/app/${a}`, 'utf8');
      for (const m of src.matchAll(/<(div|span|li|p|td)\b[^>]*\(click\)[^>]*>/g)) {
        // o fundo (scrim) da gaveta é decorativo (aria-hidden) e fecha também por Esc/botão.
        expect(m[0]).toContain('aria-hidden="true"');
      }
    }
  });

  it('foco visível global e respeito a prefers-reduced-motion', () => {
    const css: string = readFileSync('src/styles.scss', 'utf8');
    expect(css).toContain(':focus-visible');
    expect(css).toContain('prefers-reduced-motion');
  });
});

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Dialog],
  template: `
    <button id="gatilho" (click)="aberto.set(true)">Abrir</button>
    <app-dialog [(open)]="aberto" title="Confirmar">
      <button id="a">A</button>
      <button id="b">B</button>
    </app-dialog>
  `,
})
class HostDialogo {
  readonly aberto = signal(false);
}

describe('a11y — diálogo (foco preso e retorno)', () => {
  it('Tab no último volta ao primeiro, Shift+Tab no primeiro vai ao último e Esc devolve o foco', async () => {
    TestBed.configureTestingModule({ providers: [provideZonelessChangeDetection()] });
    const fixture = TestBed.createComponent(HostDialogo);
    document.body.appendChild(fixture.nativeElement);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const gatilho = el.querySelector<HTMLButtonElement>('#gatilho')!;
    gatilho.focus();
    gatilho.click();
    await fixture.whenStable();
    const painel = el.querySelector<HTMLElement>('[role=dialog]')!;
    expect(document.activeElement?.id).toBe('a');

    const tecla = (shift: boolean) =>
      painel.dispatchEvent(
        new KeyboardEvent('keydown', {
          key: 'Tab',
          shiftKey: shift,
          bubbles: true,
          cancelable: true,
        }),
      );
    el.querySelector<HTMLElement>('#b')!.focus();
    tecla(false);
    expect(document.activeElement?.id).toBe('a');
    tecla(true);
    expect(document.activeElement?.id).toBe('b');

    painel.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    await fixture.whenStable();
    expect(el.querySelector('[role=dialog]')).toBeNull();
    expect(document.activeElement?.id).toBe('gatilho');
    fixture.nativeElement.remove();
  });
});

describe('a11y — contraste dos tokens (WCAG AA)', () => {
  const css: string = readFileSync('src/styles.scss', 'utf8');
  const tokens = new Map(
    [...css.matchAll(/^\s*--color-([\w-]+):\s*(#[0-9a-fA-F]{6})\s*;/gm)].map((m) => [m[1], m[2]]),
  );
  tokens.set('white', '#ffffff');

  const lum = (hex: string): number => {
    const c = [1, 3, 5].map((i) => parseInt(hex.slice(i, i + 2), 16) / 255);
    const [r, g, b] = c.map((x) => (x <= 0.03928 ? x / 12.92 : ((x + 0.055) / 1.055) ** 2.4));
    return 0.2126 * r + 0.7152 * g + 0.0722 * b;
  };
  const razao = (a: string, b: string): number => {
    const [la, lb] = [lum(tokens.get(a)!), lum(tokens.get(b)!)].sort((x, y) => y - x);
    return (la + 0.05) / (lb + 0.05);
  };

  // [texto, fundo, mínimo] — pares realmente usados nos templates.
  const pares: readonly [string, string, number][] = [
    ['content', 'canvas', 4.5],
    ['content', 'surface', 4.5],
    ['content-muted', 'surface', 4.5],
    ['content-muted', 'canvas', 4.5],
    ['content', 'accent', 4.5],
    ['content', 'neutral-100', 4.5],
    ['content', 'neutral-200', 4.5],
    ['accent-700', 'surface', 4.5],
    ['accent-700', 'accent-100', 4.5],
    ['accent-800', 'accent-100', 4.5],
    ['accent-800', 'surface', 4.5],
    ['accent-900', 'accent-100', 4.5],
    ['white', 'accent-700', 4.5],
    ['white', 'accent-900', 4.5],
    ['accent-200', 'accent-900', 4.5],
    ['white', 'danger', 4.5],
    ['danger', 'surface', 4.5],
    ['danger', 'danger-soft', 4.5],
    ['neutral-800', 'neutral-200', 4.5],
    ['white', 'sidebar-to', 4.5],
    ['sidebar-content', 'sidebar-to', 4.5],
    ['sidebar-content', 'sidebar-from', 4.5],
    // não textuais (≥ 3:1): foco e arcos do gráfico sobre a superfície
    ['accent-700', 'canvas', 3],
    ['accent-2-700', 'surface', 3],
  ];

  for (const [t, f, min] of pares) {
    it(`${t} sobre ${f} ≥ ${min}:1`, () => {
      expect(razao(t, f)).toBeGreaterThanOrEqual(min);
    });
  }
});
