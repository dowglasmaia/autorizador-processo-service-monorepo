import { ChangeDetectionStrategy, Component } from '@angular/core';
import { EmptyState, PageHeader } from '../../../shared/ui';

/** Página-esqueleto; o conteúdo real chega nas etapas de frontend seguintes. */
@Component({
  selector: 'app-procedimentos-page',
  imports: [PageHeader, EmptyState],
  template: `
    <app-page-header kicker="Cadastro" title="Procedimentos" />
    <app-empty-state
      title="Em construção"
      description="Esta tela será entregue em uma etapa seguinte."
    />
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProcedimentosPage {}
