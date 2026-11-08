import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Card } from '../card/card';
import { Icon } from '../icon/icon';
import { Skeleton } from '../skeleton/skeleton';

@Component({
  selector: 'app-kpi-card',
  imports: [Card, Icon, Skeleton],
  template: `
    <app-card>
      <div class="flex items-start justify-between gap-3">
        <p class="text-sm font-semibold text-content-muted">{{ label() }}</p>
        @if (icon()) {
          <app-icon [name]="icon()" class="text-2xl text-accent-700" />
        }
      </div>
      @if (loading()) {
        <app-skeleton class="mt-3 h-9 w-24" />
      } @else {
        <p class="mt-2 text-3xl font-extrabold text-content">{{ value() }}</p>
      }
      @if (hint()) {
        <p class="mt-1 text-sm text-content-muted">{{ hint() }}</p>
      }
    </app-card>
  `,
  host: { class: 'block' },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KpiCard {
  readonly label = input.required<string>();
  readonly value = input<string | number>('');
  readonly hint = input('');
  readonly icon = input('');
  readonly loading = input(false);
}
