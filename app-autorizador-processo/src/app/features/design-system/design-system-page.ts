import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ErrorDialogService } from '../../core/feedback/error-dialog';
import { NotificationService } from '../../core/feedback/notification.service';
import {
  Button,
  Card,
  Chip,
  DataTable,
  DataTableColumn,
  Dialog,
  EmptyState,
  Field,
  FieldControl,
  Icon,
  KpiCard,
  PageHeader,
  SegmentedChoice,
  Skeleton,
  Tag,
} from '../../shared/ui';

interface Linha {
  readonly codigo: string;
  readonly nome: string;
}

/** Galeria de componentes do design system. Disponível apenas em desenvolvimento (`/_ds`). */
@Component({
  selector: 'app-design-system-page',
  imports: [
    Button, Card, Chip, DataTable, Dialog, EmptyState, Field, FieldControl, Icon,
    KpiCard, PageHeader, SegmentedChoice, Skeleton, Tag,
  ],
  template: `
    <app-page-header kicker="Desenvolvimento" title="Design system" description="Galeria dos componentes base." />

    <div class="grid gap-6">
      <app-card>
        <h2 class="mb-3 font-bold">Botões e tags</h2>
        <div class="flex flex-wrap items-center gap-3">
          <button app-button>Primário</button>
          <button app-button variant="secondary">Secundário</button>
          <button app-button variant="ghost">Ghost</button>
          <button app-button disabled>Desabilitado</button>
          <button app-button [loading]="true">Carregando</button>
          <app-tag tone="accent">Autorizado</app-tag>
          <app-tag tone="accent-2">Negado</app-tag>
          <app-tag>Pendente</app-tag>
          <app-icon name="seal-check" label="Ícone" />
        </div>
      </app-card>

      <div class="grid gap-4 sm:grid-cols-2">
        <app-kpi-card label="Solicitações" [value]="128" hint="no mês" icon="clipboard-text" />
        <app-kpi-card label="Carregando" [loading]="true" />
      </div>

      <app-card>
        <h2 class="mb-3 font-bold">Formulário</h2>
        <app-field label="E-mail" help="Use o e-mail corporativo." [required]="true">
          <input appFieldControl type="email" />
        </app-field>
        <app-field label="Com erro" error="Campo obrigatório.">
          <input appFieldControl />
        </app-field>
        <div class="mt-3 flex flex-wrap gap-2">
          <button app-chip [(pressed)]="chip">Filtro</button>
          <app-segmented-choice label="Tipo" [options]="opcoes" [(value)]="escolha" />
        </div>
      </app-card>

      <app-data-table [columns]="colunas" [rows]="linhas" caption="Exemplo" />
      <app-skeleton class="h-6 w-1/2" />
      <app-empty-state title="Nada por aqui" description="Estado vazio de exemplo." />

      <app-card>
        <h2 class="mb-3 font-bold">Feedback</h2>
        <div class="flex flex-wrap gap-3">
          <button app-button variant="secondary" (click)="notificacao.show('Salvo com sucesso')">Toast</button>
          <button app-button variant="secondary" (click)="erro.show('Falha de exemplo', tentarDeNovo)">Erro</button>
          <button app-button variant="secondary" (click)="dialogo.set(true)">Diálogo</button>
        </div>
      </app-card>
    </div>

    <app-dialog [(open)]="dialogo" title="Diálogo de exemplo">
      Conteúdo do diálogo.
      <button dialog-actions app-button (click)="dialogo.set(false)">Fechar</button>
    </app-dialog>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DesignSystemPage {
  protected readonly notificacao = inject(NotificationService);
  protected readonly erro = inject(ErrorDialogService);
  protected readonly dialogo = signal(false);
  protected readonly chip = signal(false);
  protected readonly escolha = signal('a');
  protected readonly opcoes = [
    { value: 'a', label: 'Opção A' },
    { value: 'b', label: 'Opção B' },
  ];
  protected readonly colunas: readonly DataTableColumn<Linha>[] = [
    { key: 'codigo', label: 'Código' },
    { key: 'nome', label: 'Nome' },
  ];
  protected readonly linhas: readonly Linha[] = [
    { codigo: '001', nome: 'Consulta' },
    { codigo: '002', nome: 'Exame' },
  ];
  protected readonly tentarDeNovo = (): void => {
    this.notificacao.show('Nova tentativa');
  };
}
