import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import type { DistribuicaoSexos } from '../../../shared/api';
import { MENSAGENS } from '../../../core/i18n/mensagens';

const RAIO = 40;
const CIRCUNFERENCIA = 2 * Math.PI * RAIO;

/** Rosca SVG de procedimentos que aceitam Masculino × Feminino, com legenda textual e total. */
@Component({
  selector: 'app-rosca-sexos',
  template: `
    <div class="flex flex-wrap items-center gap-6">
      <svg
        viewBox="0 0 100 100"
        class="size-36 shrink-0 -rotate-90"
        role="img"
        [attr.aria-label]="descricao()"
      >
        <circle
          cx="50"
          cy="50"
          [attr.r]="raio"
          fill="none"
          stroke="var(--color-neutral-200)"
          stroke-width="16"
        />
        @if (total() > 0) {
          <circle
            cx="50"
            cy="50"
            [attr.r]="raio"
            fill="none"
            stroke="var(--color-accent-700)"
            stroke-width="16"
            [attr.stroke-dasharray]="tracoMasculino() + ' ' + circunferencia"
            data-testid="arco-masculino"
          />
          <circle
            cx="50"
            cy="50"
            [attr.r]="raio"
            fill="none"
            stroke="var(--color-accent-2-700)"
            stroke-width="16"
            [attr.stroke-dasharray]="tracoFeminino() + ' ' + circunferencia"
            [attr.stroke-dashoffset]="-tracoMasculino()"
            data-testid="arco-feminino"
          />
        }
      </svg>
      <ul class="grid gap-2 text-sm">
        <li class="flex items-center gap-2">
          <span class="size-3 rounded-pill bg-accent-700" aria-hidden="true"></span>
          {{ m.masculino }}: <strong>{{ dados().masculino }}</strong>
        </li>
        <li class="flex items-center gap-2">
          <span class="size-3 rounded-pill bg-accent-2-700" aria-hidden="true"></span>
          {{ m.feminino }}: <strong>{{ dados().feminino }}</strong>
        </li>
        <li class="text-content-muted">
          {{ m.total }}: <strong class="text-content">{{ total() }}</strong>
        </li>
      </ul>
    </div>
  `,
  host: { class: 'block' },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoscaSexos {
  protected readonly m = MENSAGENS.painel;
  readonly dados = input.required<DistribuicaoSexos>();

  protected readonly raio = RAIO;
  protected readonly circunferencia = CIRCUNFERENCIA;
  protected readonly total = computed(() => this.dados().masculino + this.dados().feminino);
  protected readonly tracoMasculino = computed(() =>
    this.total() === 0 ? 0 : (this.dados().masculino / this.total()) * CIRCUNFERENCIA,
  );
  protected readonly tracoFeminino = computed(() =>
    this.total() === 0 ? 0 : (this.dados().feminino / this.total()) * CIRCUNFERENCIA,
  );
  protected readonly descricao = computed(
    () =>
      `${MENSAGENS.painel.sexosTitulo}: ${this.dados().masculino} masculino e ${this.dados().feminino} feminino, total ${this.total()}.`,
  );
}
