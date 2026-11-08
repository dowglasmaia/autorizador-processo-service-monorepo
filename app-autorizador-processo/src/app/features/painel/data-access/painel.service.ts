import { Injectable, inject, signal } from '@angular/core';
import { ApiClient } from '../../../core/http/api-client';
import { isAppError, type AppError } from '../../../core/errors/app-error';
import type { ResumoPainel } from '../../../shared/api';

/** Estado do painel e acesso à operação `obterResumoPainel` (valores calculados no servidor). */
@Injectable({ providedIn: 'root' })
export class PainelService {
  private readonly api = inject(ApiClient);

  private readonly _resumo = signal<ResumoPainel | null>(null);
  private readonly _carregando = signal(false);
  private readonly _erro = signal<AppError | null>(null);

  readonly resumo = this._resumo.asReadonly();
  readonly carregando = this._carregando.asReadonly();
  readonly erro = this._erro.asReadonly();

  /** Busca o resumo; a cada chamada os dados são atualizados (sem cache). */
  carregar(): void {
    this._carregando.set(true);
    this._erro.set(null);
    this.api.get<ResumoPainel>('painel/resumo').subscribe({
      next: (resumo) => {
        this._resumo.set(resumo);
        this._carregando.set(false);
      },
      error: (erro: unknown) => {
        this._resumo.set(null);
        this._erro.set(
          isAppError(erro)
            ? erro
            : { kind: 'servidor', detail: 'Não foi possível carregar o painel.' },
        );
        this._carregando.set(false);
      },
    });
  }
}
