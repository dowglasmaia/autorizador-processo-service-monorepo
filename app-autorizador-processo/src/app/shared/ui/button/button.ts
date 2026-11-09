import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { Icon } from '../icon/icon';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'icon';

const BASE =
  'inline-flex items-center justify-center gap-2 rounded-pill font-semibold ' +
  'transition-[transform,background-color] duration-150 ease-out active:scale-[0.97] ' +
  'aria-busy:cursor-progress disabled:cursor-not-allowed disabled:opacity-50 min-h-11';

const VARIANTS: Record<ButtonVariant, string> = {
  primary: 'bg-accent text-content px-5 py-2.5 hover:bg-accent-400',
  secondary: 'border border-divider bg-surface text-content px-5 py-2.5 hover:bg-neutral-100',
  ghost: 'bg-transparent text-accent-700 px-4 py-2.5 hover:bg-accent-100',
  icon: 'bg-transparent text-content size-11 hover:bg-neutral-100',
};

/** Botão do design system. Uso: `<button app-button variant="primary">Salvar</button>`. */
@Component({
  // eslint-disable-next-line @angular-eslint/component-selector -- atributo em <button> nativo
  selector: 'button[app-button]',
  imports: [Icon],
  template: `
    @if (loading()) {
      <app-icon name="circle-notch" class="animate-spin motion-reduce:animate-none" />
    }
    <ng-content />
  `,
  host: {
    '[class]': 'classes()',
    '[attr.aria-busy]': 'loading() ? "true" : null',
    '[attr.type]': 'type()',
  },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Button {
  readonly variant = input<ButtonVariant>('primary');
  readonly loading = input(false);
  readonly type = input<'button' | 'submit'>('button');
  protected readonly classes = computed(() => `${BASE} ${VARIANTS[this.variant()]}`);
}
