import { ChangeDetectionStrategy, Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { Button, EmptyState, Icon, KpiCard, PageHeader, Tag } from './index';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Button, Tag, Icon, KpiCard, PageHeader, EmptyState],
  template: `
    <button app-button id="b1" variant="secondary" disabled>Cancelar</button>
    <button app-button id="b2" [loading]="true">Salvar</button>
    <app-tag tone="accent">Autorizado</app-tag>
    <app-icon name="house" />
    <app-icon name="house" label="Início" />
    <app-kpi-card label="Total" [loading]="true" />
    <app-kpi-card label="Negadas" [value]="7" />
    <app-page-header kicker="Painel" title="Visão geral" />
    <app-empty-state title="Nada por aqui" />
  `,
})
class Host {}

describe('shared/ui (lote 1)', () => {
  let el: HTMLElement;

  beforeEach(async () => {
    TestBed.configureTestingModule({ providers: [provideZonelessChangeDetection()] });
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    el = fixture.nativeElement;
  });

  it('botão desabilitado e carregando', () => {
    const b1 = el.querySelector<HTMLButtonElement>('#b1')!;
    expect(b1.disabled).toBe(true);
    expect(b1.className).toContain('border-divider');
    const b2 = el.querySelector('#b2')!;
    expect(b2.getAttribute('aria-busy')).toBe('true');
    expect(b2.querySelector('app-icon')).not.toBeNull();
  });

  it('ícone decorativo some do leitor de tela; com rótulo vira imagem', () => {
    const [deco, rotulado] = Array.from(el.querySelectorAll('app-icon')).filter((i) =>
      i.classList.contains('ph-house'),
    );
    expect(deco.getAttribute('aria-hidden')).toBe('true');
    expect(rotulado.getAttribute('role')).toBe('img');
    expect(rotulado.getAttribute('aria-label')).toBe('Início');
  });

  it('tag, kpi, cabeçalho e estado vazio', () => {
    expect(el.querySelector('app-tag')!.className).toContain('text-accent-800');
    const kpis = el.querySelectorAll('app-kpi-card');
    expect(kpis[0].querySelector('app-skeleton')).not.toBeNull();
    expect(kpis[1].textContent).toContain('7');
    expect(el.querySelector('h1')!.textContent).toContain('Visão geral');
    expect(el.querySelector('app-empty-state h2')!.textContent).toContain('Nada por aqui');
  });
});
