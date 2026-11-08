import { ChangeDetectionStrategy, Component, computed, input, model } from '@angular/core';

const BASE =
  'inline-flex min-h-11 items-center justify-center gap-2 rounded-pill border px-4 py-2 text-sm font-semibold ' +
  'transition-colors disabled:cursor-not-allowed disabled:opacity-50';

/** Chip alternável. Uso: `<button app-chip [(pressed)]="ativo">Autorizados</button>`. */
@Component({
  // eslint-disable-next-line @angular-eslint/component-selector -- atributo em <button> nativo
  selector: 'button[app-chip]',
  template: '<ng-content />',
  host: {
    type: 'button',
    '[class]': 'classes()',
    '[attr.aria-pressed]': 'pressed()',
    '(click)': 'pressed.set(!pressed())',
  },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Chip {
  readonly pressed = model(false);
  protected readonly classes = computed(
    () =>
      `${BASE} ${
        this.pressed()
          ? 'border-accent-700 bg-accent-100 text-accent-800'
          : 'border-divider bg-surface text-content hover:bg-neutral-100'
      }`,
  );
}

export interface SegmentedOption {
  readonly value: string;
  readonly label: string;
}

/** Escolha segmentada de seleção única; cada opção é um botão com `aria-pressed`. */
@Component({
  selector: 'app-segmented-choice',
  template: `
    @for (option of options(); track option.value) {
      <button
        type="button"
        [class]="optionClasses(option.value === value())"
        [attr.aria-pressed]="option.value === value()"
        (click)="value.set(option.value)"
      >
        {{ option.label }}
      </button>
    }
  `,
  host: {
    role: 'group',
    '[attr.aria-label]': 'label() || null',
    class: 'inline-flex flex-wrap gap-1 rounded-pill border border-divider bg-neutral-100 p-1',
  },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SegmentedChoice {
  readonly options = input.required<readonly SegmentedOption[]>();
  readonly value = model<string>('');
  readonly label = input('');

  protected optionClasses(active: boolean): string {
    return (
      'min-h-11 rounded-pill px-4 py-2 text-sm font-semibold transition-colors ' +
      (active ? 'bg-accent text-content' : 'bg-transparent text-content hover:bg-neutral-200')
    );
  }
}
