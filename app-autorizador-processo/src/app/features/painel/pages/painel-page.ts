import { ChangeDetectionStrategy, Component } from '@angular/core';
import { EmptyState, PageHeader } from '../../../shared/ui';

/** Página-esqueleto; o conteúdo real chega nas etapas de frontend seguintes. */
@Component({
  selector: 'app-painel-page',
  imports: [PageHeader, EmptyState],
  template: `
    <app-page-header kicker="Visão geral" title="Painel" />
    <app-empty-state
      title="Em construção"
      description="Esta tela será entregue em uma etapa seguinte."
    />
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PainelPage {}
