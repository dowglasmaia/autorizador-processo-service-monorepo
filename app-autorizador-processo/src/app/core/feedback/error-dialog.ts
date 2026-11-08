import { ChangeDetectionStrategy, Component, Injectable, inject, signal } from '@angular/core';
import { Button, Dialog } from '../../shared/ui';
import { MENSAGENS } from '../../core/i18n/mensagens';

export interface ErrorDialogState {
  readonly message: string;
  readonly retry?: () => void;
}

/** Abre o diálogo de erro de carregamento. `retry` habilita o botão "Tentar novamente". */
@Injectable({ providedIn: 'root' })
export class ErrorDialogService {
  private readonly _state = signal<ErrorDialogState | null>(null);
  readonly state = this._state.asReadonly();

  show(message: string, retry?: () => void): void {
    this._state.set({ message, retry });
  }

  close(): void {
    this._state.set(null);
  }

  retry(): void {
    const acao = this._state()?.retry;
    this.close();
    acao?.();
  }
}

@Component({
  selector: 'app-error-dialog',
  imports: [Dialog, Button],
  template: `
    <app-dialog
      [title]="m.feedback.tituloErroCarregar"
      [open]="service.state() !== null"
      (closed)="service.close()"
    >
      <p>{{ service.state()?.message }}</p>
      <ng-container dialog-actions>
        <button app-button variant="secondary" (click)="service.close()">{{ m.acoes.fechar }}</button>
        @if (service.state()?.retry) {
          <button app-button (click)="service.retry()">{{ m.acoes.tentarNovamente }}</button>
        }
      </ng-container>
    </app-dialog>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ErrorDialog {
  protected readonly service = inject(ErrorDialogService);
  protected readonly m = MENSAGENS;
}
