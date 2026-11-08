import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import {
  Button,
  DataTable,
  DataTableCell,
  EmptyState,
  PageHeader,
  Skeleton,
  Tag,
} from '../../../shared/ui';
import type { DataTableColumn } from '../../../shared/ui';
import type { ProcedimentoDto } from '../../../shared/api';
import { ProcedimentosService } from '../data-access/procedimentos.service';
import { ROTULO_CATEGORIA, ROTULO_SEXO } from '../rotulos';
import { MENSAGENS } from '../../../core/i18n/mensagens';

/** Lista de procedimentos: esqueleto, erro, vazio com CTA e tabela responsiva. */
@Component({
  selector: 'app-procedimentos-page',
  imports: [RouterLink, PageHeader, EmptyState, Skeleton, Button, Tag, DataTable, DataTableCell],
  template: `
    <app-page-header
      [kicker]="m.kicker"
      [title]="m.titulo"
      [description]="m.descricao"
    >
      <button actions app-button (click)="novo()">{{ acoes.novoProcedimento }}</button>
    </app-page-header>

    <section class="mt-6" aria-live="polite">
      @if (servico.carregando()) {
        <div class="grid gap-3" role="status" [attr.aria-label]="m.carregando">
          @for (i of esqueleto; track i) {
            <app-skeleton class="h-12 w-full" />
          }
        </div>
      } @else if (servico.erro(); as erro) {
        <app-empty-state
          icon="warning"
          [title]="feedback.tituloErroCarregar"
          [description]="erro.detail"
        >
          <button app-button variant="secondary" (click)="servico.carregar()">
            {{ acoes.tentarNovamente }}
          </button>
        </app-empty-state>
      } @else if (servico.vazio()) {
        <app-empty-state
          [title]="m.vazioTitulo"
          [description]="m.vazioDescricao"
        >
          <button app-button (click)="novo()">{{ acoes.novoProcedimento }}</button>
        </app-empty-state>
      } @else {
        <app-data-table [columns]="colunas" [rows]="servico.itens()" [caption]="m.titulo">
          <ng-template appCell let-row let-column="column" let-text="text">
            @switch (column.key) {
              @case ('procedimento') {
                <span class="font-semibold">{{ row.numProcedimento }}</span>
                @if (row.nome) {
                  · {{ row.nome }}
                } @else {
                  · <span class="italic text-content-muted">{{ comum.semNome }}</span>
                }
              }
              @case ('sexos') {
                <span class="flex flex-wrap gap-1">
                  @for (s of row.sexos; track s) {
                    <app-tag tone="accent">{{ rotuloSexo(s) }}</app-tag>
                  }
                </span>
              }
              @case ('acoes') {
                <a
                  class="inline-flex min-h-11 items-center font-semibold text-accent-700 hover:underline"
                  routerLink="/solicitar"
                  [queryParams]="{ numero: row.numProcedimento }"
                  [attr.aria-label]="'Solicitar autorização do procedimento ' + row.numProcedimento"
                  >{{ acoes.solicitar }}</a
                >
              }
              @default {
                {{ text }}
              }
            }
          </ng-template>
        </app-data-table>
      }
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProcedimentosPage implements OnInit {
  protected readonly servico = inject(ProcedimentosService);
  private readonly router = inject(Router);
  protected readonly esqueleto = [1, 2, 3];
  protected readonly m = MENSAGENS.procedimentos;
  protected readonly acoes = MENSAGENS.acoes;
  protected readonly feedback = MENSAGENS.feedback;
  protected readonly comum = MENSAGENS.comum;

  protected readonly colunas: readonly DataTableColumn<ProcedimentoDto>[] = [
    { key: 'procedimento', label: MENSAGENS.procedimentos.colunas.procedimento },
    {
      key: 'categoria',
      label: MENSAGENS.procedimentos.colunas.categoria,
      value: (p) => (p.categoria ? ROTULO_CATEGORIA[p.categoria] : null),
    },
    { key: 'sexos', label: MENSAGENS.procedimentos.colunas.sexos },
    { key: 'idades', label: MENSAGENS.procedimentos.colunas.idades, value: (p) => resumoIdades(p.idades) },
    { key: 'id', label: MENSAGENS.procedimentos.colunas.id },
    { key: 'acoes', label: MENSAGENS.procedimentos.colunas.acao },
  ];

  ngOnInit(): void {
    this.servico.carregar({ pagina: 0 });
  }

  protected novo(): void {
    void this.router.navigate(['/procedimentos', 'novo']);
  }

  protected rotuloSexo(s: 'M' | 'F'): string {
    return ROTULO_SEXO[s];
  }
}

/** "18 a 20 anos" para faixa contínua; lista curta caso contrário. */
export function resumoIdades(idades: readonly number[]): string {
  if (idades.length === 0) return '—';
  const min = idades[0];
  const max = idades[idades.length - 1];
  const continua = max - min + 1 === idades.length;
  if (idades.length === 1) return `${min} anos`;
  return continua ? `${min} a ${max} anos` : `${idades.length} idades (${min}–${max})`;
}
