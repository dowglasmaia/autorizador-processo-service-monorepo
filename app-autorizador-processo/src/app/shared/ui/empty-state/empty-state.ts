import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Icon } from '../icon/icon';

@Component({
  selector: 'app-empty-state',
  imports: [Icon],
  template: `
    <app-icon [name]="icon()" class="text-4xl text-accent-700" />
    <h2 class="mt-3 text-lg font-bold text-content">{{ title() }}</h2>
    @if (description()) {
      <p class="mt-1 max-w-prose text-content-muted">{{ description() }}</p>
    }
    <div class="mt-4"><ng-content /></div>
  `,
  host: {
    class: 'flex flex-col items-center rounded-card border border-dashed border-divider bg-surface p-8 text-center',
  },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmptyState {
  readonly title = input.required<string>();
  readonly description = input('');
  readonly icon = input('tray');
}
