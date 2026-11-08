import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/** Kicker + título + descrição + ação (slot `[actions]`). */
@Component({
  selector: 'app-page-header',
  template: `
    <div>
      @if (kicker()) {
        <p class="text-xs font-bold uppercase tracking-wider text-accent-700">{{ kicker() }}</p>
      }
      <h1 class="text-2xl font-extrabold text-content sm:text-3xl">{{ title() }}</h1>
      @if (description()) {
        <p class="mt-1 max-w-prose text-content-muted">{{ description() }}</p>
      }
    </div>
    <div class="flex flex-wrap gap-2"><ng-content select="[actions]" /></div>
  `,
  host: { class: 'flex flex-wrap items-end justify-between gap-4' },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PageHeader {
  readonly title = input.required<string>();
  readonly kicker = input('');
  readonly description = input('');
}
