import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EmptyState } from '../../shared/ui';
import { MENSAGENS } from '../../core/i18n/mensagens';

@Component({
  selector: 'app-nao-encontrada-page',
  imports: [EmptyState, RouterLink],
  template: `
    <app-empty-state
      [title]="m.titulo"
      [description]="m.descricao"
      icon="warning"
    >
      <a
        routerLink="/painel"
        class="inline-flex min-h-11 items-center font-semibold text-accent-800 underline"
      >{{ acoes.voltarAoPainel }}</a>
    </app-empty-state>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NaoEncontradaPage {
  protected readonly m = MENSAGENS.naoEncontrada;
  protected readonly acoes = MENSAGENS.acoes;
}
