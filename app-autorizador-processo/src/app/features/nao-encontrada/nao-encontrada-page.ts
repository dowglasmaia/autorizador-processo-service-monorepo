import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EmptyState } from '../../shared/ui';

@Component({
  selector: 'app-nao-encontrada-page',
  imports: [EmptyState, RouterLink],
  template: `
    <app-empty-state title="Página não encontrada" description="O endereço acessado não existe." icon="warning">
      <a routerLink="/painel" class="font-semibold text-accent-800 underline">Voltar ao painel</a>
    </app-empty-state>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NaoEncontradaPage {}
