import { Injectable, signal } from '@angular/core';

/** Contadores de solicitações da sessão (só em memória; perdidos ao recarregar a página). */
@Injectable({ providedIn: 'root' })
export class SessaoSolicitacoes {
  private readonly _concedidas = signal(0);
  private readonly _negadas = signal(0);

  readonly concedidas = this._concedidas.asReadonly();
  readonly negadas = this._negadas.asReadonly();

  registrar(resultado: 'autorizado' | 'negado'): void {
    if (resultado === 'autorizado') this._concedidas.update((n) => n + 1);
    else this._negadas.update((n) => n + 1);
  }
}
