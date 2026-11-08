import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import type { CoberturaIdade } from '../../../shared/api';
import { MENSAGENS } from '../../../core/i18n/mensagens';

/** Barras horizontais: largura proporcional ao maior valor devolvido pelo servidor. */
@Component({
  selector: 'app-barra-cobertura',
  template: `
    <ul class="grid gap-3" aria-hidden="true">
      @for (b of barras(); track b.idade) {
        <li class="grid grid-cols-[4.5rem_1fr_2rem] items-center gap-3 text-sm">
          <span class="font-semibold text-content">{{ b.idade }} anos</span>
          <span class="h-3 overflow-hidden rounded-pill bg-neutral-200">
            <span
              class="block h-full rounded-pill bg-accent-700 transition-[width] duration-500 motion-reduce:transition-none"
              [style.width.%]="b.largura"
              data-testid="barra"
            ></span>
          </span>
          <span class="text-right font-bold text-content">{{ b.quantidade }}</span>
        </li>
      }
    </ul>
    <table class="sr-only">
      <caption>
        {{ m.coberturaTabela }}
      </caption>
      <thead>
        <tr>
          <th scope="col">{{ m.colunaIdade }}</th>
          <th scope="col">{{ m.colunaProcedimentos }}</th>
        </tr>
      </thead>
      <tbody>
        @for (b of barras(); track b.idade) {
          <tr>
            <th scope="row">{{ b.idade }} anos</th>
            <td>{{ b.quantidade }}</td>
          </tr>
        }
      </tbody>
    </table>
  `,
  host: { class: 'block' },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BarraCobertura {
  protected readonly m = MENSAGENS.painel;
  readonly dados = input.required<readonly CoberturaIdade[]>();

  protected readonly barras = computed(() => {
    const maximo = Math.max(0, ...this.dados().map((d) => d.quantidade));
    return this.dados().map((d) => ({
      ...d,
      largura: maximo === 0 ? 0 : (d.quantidade / maximo) * 100,
    }));
  });
}
