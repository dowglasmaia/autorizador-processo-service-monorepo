import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { Chip, DataTable, DataTableColumn, Dialog, Field, FieldControl } from './index';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Field, FieldControl, Chip, DataTable, Dialog],
  template: `
    <app-field label="E-mail" help="Ajuda" error="Obrigatório"
      ><input appFieldControl id="i1"
    /></app-field>
    <app-field label="Nome" help="Ajuda"><input appFieldControl id="i2" /></app-field>
    <button app-chip id="c" [(pressed)]="pressionado">Filtro</button>
    <app-data-table [columns]="colunas" [rows]="linhas" />
    <button id="abrir" (click)="aberto.set(true)">Abrir</button>
    <app-dialog [(open)]="aberto" title="Confirmar"><button id="ok">OK</button></app-dialog>
  `,
})
class Host {
  readonly pressionado = signal(false);
  readonly aberto = signal(false);
  readonly colunas: readonly DataTableColumn<{ nome: string }>[] = [{ key: 'nome', label: 'Nome' }];
  readonly linhas = [{ nome: 'Ana' }];
}

describe('shared/ui (lote 3)', () => {
  let fixture: ReturnType<typeof TestBed.createComponent<Host>>;
  let el: HTMLElement;

  beforeEach(async () => {
    TestBed.configureTestingModule({ providers: [provideZonelessChangeDetection()] });
    fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    el = fixture.nativeElement;
  });

  it('campo com erro: aria-invalid e aria-describedby apontam para a mensagem', () => {
    const i1 = el.querySelectorAll('input')[0];
    expect(i1.getAttribute('aria-invalid')).toBe('true');
    const ids = (i1.getAttribute('aria-describedby') ?? '').split(' ').filter(Boolean);
    const textos = ids.map((id) => el.querySelector(`[id="${id}"]`)?.textContent ?? '');
    expect(textos.some((t) => t.includes('Obrigatório'))).toBe(true);
    expect(el.querySelectorAll('input')[1].getAttribute('aria-invalid')).not.toBe('true');
  });

  it('chip alterna aria-pressed', async () => {
    const chip = el.querySelector<HTMLButtonElement>('#c')!;
    expect(chip.getAttribute('aria-pressed')).toBe('false');
    chip.click();
    await fixture.whenStable();
    expect(chip.getAttribute('aria-pressed')).toBe('true');
  });

  it('tabela e cartões coexistem, alternados por classes responsivas', () => {
    expect(el.querySelector('table')!.parentElement!.className).toContain('md:block');
    expect(el.querySelector('ul')!.className).toContain('md:hidden');
    expect(el.querySelector('dt')!.textContent).toContain('Nome');
  });

  it('diálogo: abre com role, Esc fecha e o foco volta ao botão de origem', async () => {
    const abrir = el.querySelector<HTMLButtonElement>('#abrir')!;
    abrir.focus();
    abrir.click();
    await fixture.whenStable();
    const painel = el.querySelector('[role="dialog"]')!;
    expect(painel.getAttribute('aria-modal')).toBe('true');
    painel.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    await fixture.whenStable();
    expect(el.querySelector('[role="dialog"]')).toBeNull();
    expect(document.activeElement).toBe(abrir);
  });
});
