import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import type { AutorizacaoDto } from '../../../shared/api';
import { descreverRegras } from '../../autorizacoes/regras';
import { MENSAGENS } from '../../../core/i18n/mensagens';

const MAXIMO = 4;

/** Até 4 autorizações recentes, com regras via `descreverRegras` e link para o histórico. */
@Component({
  selector: 'app-ultimas-autorizacoes',
  imports: [DatePipe, RouterLink],
  template: `
    <div class="flex items-center justify-between gap-3">
      <h2 class="text-lg font-bold text-content">{{ m.ultimasTitulo }}</h2>
      <a
        routerLink="/autorizacoes"
        class="inline-flex min-h-11 items-center text-sm font-semibold text-accent-700 hover:underline"
      >
        {{ acoes.verTodas }}
      </a>
    </div>
    @if (itens().length === 0) {
      <p class="mt-4 text-content-muted">{{ m.ultimasVazia }}</p>
    } @else {
      <ul class="mt-4 grid gap-3">
        @for (a of itens(); track a.id) {
          <li class="rounded-input border border-divider p-3">
            <p class="font-semibold text-content">
              Procedimento {{ a.procedimento.numProcedimento }}
              @if (a.procedimento.nome) {
                · {{ a.procedimento.nome }}
              }
            </p>
            <p class="text-sm text-content-muted">{{ regras(a) }}</p>
            <p class="text-xs text-content-muted">
              @if (a.criadaEm) {
                {{ a.criadaEm | date: 'dd/MM/yyyy HH:mm' }}
              } @else {
                —
              }
            </p>
          </li>
        }
      </ul>
    }
  `,
  host: { class: 'block' },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UltimasAutorizacoes {
  protected readonly m = MENSAGENS.painel;
  protected readonly acoes = MENSAGENS.acoes;
  readonly autorizacoes = input.required<readonly AutorizacaoDto[]>();

  protected readonly itens = computed(() => this.autorizacoes().slice(0, MAXIMO));
  protected readonly regras = (a: AutorizacaoDto): string => descreverRegras(a.procedimento);
}
