import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
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
import type { AutorizacaoDto } from '../../../shared/api';
import { AutorizacoesService } from '../data-access/autorizacoes.service';
import { descreverRegras } from '../regras';

/** Histórico de autorizações concedidas: esqueleto, erro, vazio com CTA e tabela responsiva. */
@Component({
  selector: 'app-autorizacoes-page',
  imports: [PageHeader, EmptyState, Skeleton, Button, Tag, DataTable, DataTableCell, DatePipe],
  template: `
    <app-page-header
      kicker="Histórico"
      title="Autorizações concedidas"
      description="Somente aprovações são registradas — negativas não deixam rastro."
    >
      <button actions app-button (click)="nova()">Nova solicitação</button>
    </app-page-header>

    <section class="mt-6" aria-live="polite">
      @if (servico.carregando()) {
        <div class="grid gap-3" role="status" aria-label="Carregando autorizações">
          @for (i of esqueleto; track i) {
            <app-skeleton class="h-12 w-full" />
          }
        </div>
      } @else if (servico.erro(); as erro) {
        <app-empty-state
          icon="warning"
          title="Não foi possível carregar"
          [description]="erro.detail"
        >
          <button app-button variant="secondary" (click)="servico.listarAutorizacoes()">
            Tentar novamente
          </button>
        </app-empty-state>
      } @else if (servico.itens().length === 0) {
        <app-empty-state
          title="Nada por aqui ainda"
          description="As autorizações concedidas aparecerão aqui."
        >
          <button app-button (click)="nova()">Solicitar a primeira</button>
        </app-empty-state>
      } @else {
        <app-data-table
          [columns]="colunas"
          [rows]="servico.itens()"
          caption="Autorizações concedidas"
        >
          <ng-template appCell let-row let-column="column" let-text="text">
            @switch (column.key) {
              @case ('procedimento') {
                <span class="font-semibold">{{ row.procedimento.numProcedimento }}</span>
                @if (row.procedimento.nome) {
                  · {{ row.procedimento.nome }}
                }
              }
              @case ('status') {
                <app-tag tone="accent">Autorizado</app-tag>
              }
              @case ('data') {
                @if (row.criadaEm) {
                  {{ row.criadaEm | date: 'dd/MM/yyyy HH:mm' }}
                } @else {
                  —
                }
              }
              @default {
                {{ text }}
              }
            }
          </ng-template>
        </app-data-table>
        <nav class="mt-4 flex items-center justify-between gap-3" aria-label="Paginação">
          <button
            app-button
            variant="secondary"
            type="button"
            [disabled]="servico.pagina() <= 0"
            (click)="irPara(servico.pagina() - 1)"
          >
            Anterior
          </button>
          <span class="text-sm" aria-live="polite" data-testid="pagina-atual">
            Página {{ servico.pagina() + 1 }} de {{ servico.totalPaginas() }}
          </span>
          <button
            app-button
            variant="secondary"
            type="button"
            [disabled]="servico.pagina() + 1 >= servico.totalPaginas()"
            (click)="irPara(servico.pagina() + 1)"
          >
            Próxima
          </button>
        </nav>
      }
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AutorizacoesPage implements OnInit {
  protected readonly servico = inject(AutorizacoesService);
  private readonly router = inject(Router);
  protected readonly esqueleto = [1, 2, 3];

  protected readonly colunas: readonly DataTableColumn<AutorizacaoDto>[] = [
    { key: 'id', label: 'ID' },
    { key: 'procedimento', label: 'Procedimento' },
    {
      key: 'regras',
      label: 'Regras do procedimento',
      value: (a) => descreverRegras(a.procedimento),
    },
    { key: 'status', label: 'Status' },
    { key: 'data', label: 'Data' },
  ];

  ngOnInit(): void {
    this.servico.listarAutorizacoes(0);
  }

  protected irPara(pagina: number): void {
    if (pagina < 0 || pagina >= this.servico.totalPaginas()) return;
    this.servico.listarAutorizacoes(pagina);
  }

  protected nova(): void {
    void this.router.navigate(['/solicitar']);
  }
}
