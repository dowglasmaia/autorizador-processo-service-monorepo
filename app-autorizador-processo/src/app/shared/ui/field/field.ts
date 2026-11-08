import {
  ChangeDetectionStrategy,
  Component,
  Directive,
  computed,
  inject,
  input,
} from '@angular/core';

let proximoId = 0;

/**
 * Campo de formulário com rótulo persistente, ajuda e erro.
 * Uso: `<app-field label="E-mail" [error]="msg"><input appFieldControl formControlName="email" /></app-field>`.
 * Compatível com Reactive Forms: o controle projetado mantém o seu `formControl`.
 */
@Component({
  selector: 'app-field',
  template: `
    <label class="mb-1.5 block text-sm font-semibold text-content" [attr.for]="controlId">
      {{ label() }}
      @if (required()) {
        <span class="text-danger" aria-hidden="true">*</span>
      }
    </label>
    <ng-content />
    @if (help()) {
      <p class="mt-1.5 text-sm text-content-muted" [id]="helpId">{{ help() }}</p>
    }
    @if (error()) {
      <p class="mt-1.5 text-sm font-semibold text-danger" [id]="errorId">{{ error() }}</p>
    }
  `,
  host: { class: 'block' },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Field {
  readonly label = input.required<string>();
  readonly help = input('');
  readonly error = input('');
  readonly required = input(false);

  private readonly uid = `app-field-${++proximoId}`;
  readonly controlId = `${this.uid}-control`;
  readonly helpId = `${this.uid}-help`;
  readonly errorId = `${this.uid}-error`;

  /** Ids que descrevem o controle (ajuda e/ou erro). */
  readonly describedBy = computed(() => {
    const ids = [this.help() ? this.helpId : '', this.error() ? this.errorId : ''].filter(Boolean);
    return ids.length ? ids.join(' ') : null;
  });
}

/** Liga o `input`/`select`/`textarea` ao `Field` pai (id, `aria-describedby`, `aria-invalid`). */
@Directive({
  selector: '[appFieldControl]',
  host: {
    '[id]': 'field.controlId',
    '[attr.aria-describedby]': 'field.describedBy()',
    '[attr.aria-invalid]': 'field.error() ? "true" : null',
    '[attr.aria-required]': 'field.required() ? "true" : null',
    class:
      'block min-h-11 w-full rounded-input border border-divider bg-surface px-3.5 py-2.5 text-content ' +
      'placeholder:text-content-muted disabled:cursor-not-allowed disabled:opacity-50 ' +
      'aria-[invalid=true]:border-danger',
  },
})
export class FieldControl {
  protected readonly field = inject(Field);
}
