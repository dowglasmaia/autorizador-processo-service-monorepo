import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/** Ícone Phosphor duotone empacotado (sem CDN). `name` sem o prefixo `ph-`, ex.: `house`. */
@Component({
  selector: 'app-icon',
  template: '',
  host: {
    '[class]': 'classes()',
    '[attr.aria-hidden]': 'label() ? null : "true"',
    '[attr.role]': 'label() ? "img" : null',
    '[attr.aria-label]': 'label() || null',
  },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Icon {
  readonly name = input.required<string>();
  readonly label = input<string>('');
  protected readonly classes = computed(
    () => `ph-duotone ph-${this.name()} inline-block align-middle`,
  );
}
