import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiClient } from '../../../core/http/api-client';
import { isAppError, type AppError } from '../../../core/errors/app-error';
import type {
  PaginaProcedimentos,
  ProcedimentoDto,
  ProcedimentoRequest,
} from '../../../shared/api';

export const TAMANHO_PAGINA = 20;

/** Estado da lista de procedimentos e acesso às operações `listarProcedimentos` e `cadastrarProcedimento`. */
@Injectable({ providedIn: 'root' })
export class ProcedimentosService {
  private readonly api = inject(ApiClient);

  private readonly _itens = signal<readonly ProcedimentoDto[]>([]);
  private readonly _carregando = signal(false);
  private readonly _erro = signal<AppError | null>(null);
  private readonly _pagina = signal(0);
  private readonly _totalPaginas = signal(0);
  private readonly _totalItens = signal(0);
  private readonly _q = signal('');

  readonly itens = this._itens.asReadonly();
  readonly carregando = this._carregando.asReadonly();
  readonly erro = this._erro.asReadonly();
  readonly pagina = this._pagina.asReadonly();
  readonly totalPaginas = this._totalPaginas.asReadonly();
  readonly totalItens = this._totalItens.asReadonly();
  readonly q = this._q.asReadonly();
  readonly vazio = computed(
    () => !this._carregando() && this._erro() === null && this._itens().length === 0,
  );

  /** Carrega uma página; `q` filtra por prefixo de `numProcedimento`. Sem argumentos, mantém busca e página atuais. */
  carregar(opcoes: { q?: string; pagina?: number } = {}): void {
    const q = opcoes.q ?? this._q();
    const pagina = opcoes.pagina ?? (opcoes.q === undefined ? this._pagina() : 0);
    this._q.set(q);
    this._carregando.set(true);
    this._erro.set(null);
    this.listarProcedimentos(q, pagina).subscribe({
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
            : { kind: 'servidor', detail: 'Não foi possível carregar os procedimentos.' },
        );
        this._carregando.set(false);
      },
    });
  }

  listarProcedimentos(q: string, pagina: number): Observable<PaginaProcedimentos> {
    const busca = q.trim();
    return this.api.get<PaginaProcedimentos>('procedimentos', {
      q: busca === '' ? undefined : busca,
      page: pagina,
      size: TAMANHO_PAGINA,
      sort: 'numProcedimento,asc',
    });
  }

  cadastrarProcedimento(corpo: ProcedimentoRequest): Observable<ProcedimentoDto> {
    return this.api
      .post<ProcedimentoDto, ProcedimentoRequest>('procedimentos', corpo)
      .pipe(tap(() => this._pagina.set(0)));
  }
}
