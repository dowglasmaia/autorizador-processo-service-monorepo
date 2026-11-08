import {
  ChangeDetectionStrategy,
  Component,
  Directive,
  TemplateRef,
  contentChild,
  inject,
  input,
  viewChild,
} from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';

export interface DataTableColumn<T> {
  readonly key: string;
  readonly label: string;
  /** Extrai o texto da célula; padrão: `row[key]`. */
  readonly value?: (row: T) => string | number | null | undefined;
}

/** Template opcional de célula: `<ng-template appCell let-row let-column="column">`. */
@Directive({ selector: 'ng-template[appCell]' })
export class DataTableCell {
  readonly template = inject(TemplateRef);
}

/**
 * Tabela responsiva: tabela real a partir de `md`; cartões com rótulo por campo abaixo disso.
 * Os dois layouts são alternados por CSS (`hidden md:block` / `md:hidden`).
 */
@Component({
  selector: 'app-data-table',
  imports: [NgTemplateOutlet],
  template: `
    <div class="hidden overflow-x-auto md:block">
      <table class="w-full border-collapse text-left text-sm">
        <caption class="sr-only">
          {{ caption() }}
        </caption>
        <thead>
          <tr class="border-b border-divider text-content-muted">
            @for (col of columns(); track col.key) {
              <th scope="col" class="px-4 py-3 font-semibold">{{ col.label }}</th>
            }
          </tr>
        </thead>
        <tbody>
          @for (row of rows(); track $index) {
            <tr class="border-b border-divider last:border-b-0">
              @for (col of columns(); track col.key) {
                <td class="px-4 py-3 text-content">
                  <ng-container *ngTemplateOutlet="cellTpl(); context: ctx(row, col)" />
                </td>
              }
            </tr>
          }
        </tbody>
      </table>
    </div>

    <ul class="grid gap-3 md:hidden" [attr.aria-label]="caption()">
      @for (row of rows(); track $index) {
        <li class="rounded-card border border-divider bg-surface p-4">
          <dl class="grid gap-2">
            @for (col of columns(); track col.key) {
              <div class="flex flex-col">
                <dt class="text-xs font-semibold uppercase tracking-wide text-content-muted">
                  {{ col.label }}
                </dt>
                <dd class="text-content">
                  <ng-container *ngTemplateOutlet="cellTpl(); context: ctx(row, col)" />
                </dd>
              </div>
            }
          </dl>
        </li>
      }
    </ul>

    <ng-template #padrao let-text="text">{{ text }}</ng-template>
  `,
  host: { class: 'block' },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DataTable<T = Record<string, unknown>> {
  readonly columns = input.required<readonly DataTableColumn<T>[]>();
  readonly rows = input.required<readonly T[]>();
  /** Legenda acessível da tabela/lista. */
  readonly caption = input('Tabela de dados');

  private readonly customCell = contentChild(DataTableCell);
  private readonly defaultCell = viewChild.required<TemplateRef<unknown>>('padrao');

  protected cellTpl(): TemplateRef<unknown> {
    return this.customCell()?.template ?? this.defaultCell();
  }

  protected ctx(row: T, col: DataTableColumn<T>): Record<string, unknown> {
    const raw = col.value ? col.value(row) : (row as Record<string, unknown>)[col.key];
    return { $implicit: row, column: col, text: raw ?? '—' };
  }
}
