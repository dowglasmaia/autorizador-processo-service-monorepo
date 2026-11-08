import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { SegmentedChoice } from './chip/chip';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SegmentedChoice],
  template: `<app-segmented-choice label="Sexo" [options]="opcoes" [(value)]="valor" />`,
})
class Host {
  readonly opcoes = [
    { value: 'M', label: 'Masculino' },
    { value: 'F', label: 'Feminino' },
  ];
  readonly valor = signal('');
}

describe('SegmentedChoice', () => {
  it('expõe grupo rotulado, botões nativos (teclado) e aria-pressed', async () => {
    TestBed.configureTestingModule({ providers: [provideZonelessChangeDetection()] });
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const el: HTMLElement = fixture.nativeElement;
    const grupo = el.querySelector('app-segmented-choice')!;
    expect(grupo.getAttribute('role')).toBe('group');
    expect(grupo.getAttribute('aria-label')).toBe('Sexo');

    const botoes = Array.from(el.querySelectorAll<HTMLButtonElement>('button'));
    // <button type="button"> é focável e ativável por Enter/Espaço sem código extra.
    expect(botoes.every((b) => b.tagName === 'BUTTON' && b.type === 'button')).toBe(true);
    expect(botoes.map((b) => b.getAttribute('aria-pressed'))).toEqual(['false', 'false']);

    botoes[1].click();
    await fixture.whenStable();
    expect(fixture.componentInstance.valor()).toBe('F');
    expect(botoes.map((b) => b.getAttribute('aria-pressed'))).toEqual(['false', 'true']);
  });
});
