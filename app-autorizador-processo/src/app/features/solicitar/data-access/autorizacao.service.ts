import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiClient } from '../../../core/http/api-client';
import { resultadoNegocio } from '../../../core/http/resultado-negocio';
import type {
  AutorizacaoDto,
  AutorizacaoRequest,
  PaginaProcedimentos,
  ProcedimentoDto,
} from '../../../shared/api';

/** Quantidade máxima de procedimentos carregados para o seletor. */
export const TAMANHO_SELETOR = 200;

export type ResultadoSolicitacao =
  | { readonly resultado: 'autorizado'; readonly autorizacao: AutorizacaoDto }
  | { readonly resultado: 'negado'; readonly detail: string };

/**
 * Operações `solicitarAutorizacao` e `listarProcedimentos` (leitura do seletor) da tela de solicitação.
 * Não guarda estado: dados do paciente não são armazenados.
 */
@Injectable({ providedIn: 'root' })
export class AutorizacaoService {
  private readonly api = inject(ApiClient);

  /** `201` → autorizado; `403 AUTORIZACAO_NEGADA` → negado; demais erros propagam como `AppError`. */
  solicitarAutorizacao(corpo: AutorizacaoRequest): Observable<ResultadoSolicitacao> {
    return this.api.post<AutorizacaoDto, AutorizacaoRequest>('autorizacoes', corpo).pipe(
      resultadoNegocio(),
      map((r): ResultadoSolicitacao =>
        r.resultado === 'ok'
          ? { resultado: 'autorizado', autorizacao: r.valor }
          : { resultado: 'negado', detail: r.detail },
      ),
    );
  }

  /** Lista para o seletor: até 200 itens por `numProcedimento` crescente, sem tocar o estado de `ProcedimentosService`. */
  listarProcedimentos(): Observable<readonly ProcedimentoDto[]> {
    return this.api
      .get<PaginaProcedimentos>('procedimentos', {
        size: TAMANHO_SELETOR,
        sort: 'numProcedimento,asc',
      })
      .pipe(map((pagina) => pagina.itens));
  }
}
