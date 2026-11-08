import { Injectable, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClient } from '../../../core/http/api-client';
import { isAppError, type AppError } from '../../../core/errors/app-error';
import type { AutorizacaoDto, PaginaAutorizacoes } from '../../../shared/api';

export const TAMANHO_PAGINA = 20;

/** Estado do histórico e acesso à operação `listarAutorizacoes` (mais recentes primeiro, ordem do backend). */
@Injectable({ providedIn: 'root' })
export class AutorizacoesService {
  private readonly api = inject(ApiClient);

  private readonly _itens = signal<readonly AutorizacaoDto[]>([]);
  private readonly _carregando = signal(false);
  private readonly _erro = signal<AppError | null>(null);
  private readonly _pagina = signal(0);
  private readonly _totalPaginas = signal(0);
  private readonly _totalItens = signal(0);

  readonly itens = this._itens.asReadonly();
  readonly carregando = this._carregando.asReadonly();
  readonly erro = this._erro.asReadonly();
  readonly pagina = this._pagina.asReadonly();
  readonly totalPaginas = this._totalPaginas.asReadonly();
  readonly totalItens = this._totalItens.asReadonly();

  /** Carrega a página indicada (base 0); sem argumento, mantém a página atual. */
  listarAutorizacoes(pagina: number = this._pagina()): void {
    this._carregando.set(true);
    this._erro.set(null);
    this.buscar(pagina).subscribe({
      next: (resposta) => {
        this._itens.set(resposta.itens);
        this._pagina.set(resposta.pagina);
        this._totalPaginas.set(resposta.totalPaginas);
        this._totalItens.set(resposta.totalItens);
        this._carregando.set(false);
      },
      error: (erro: unknown) => {
        this._itens.set([]);
        this._erro.set(
          isAppError(erro)
            ? erro
            : { kind: 'servidor', detail: 'Não foi possível carregar as autorizações.' },
        );
        this._carregando.set(false);
      },
    });
  }

  private buscar(pagina: number): Observable<PaginaAutorizacoes> {
    return this.api.get<PaginaAutorizacoes>('autorizacoes', { page: pagina, size: TAMANHO_PAGINA });
  }
}
