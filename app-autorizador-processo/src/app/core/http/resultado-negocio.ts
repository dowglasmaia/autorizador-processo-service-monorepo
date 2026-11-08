import { Observable, OperatorFunction, catchError, map, of, throwError } from 'rxjs';
import { isAppError } from '../errors/app-error';

export type ResultadoNegocio<T> =
  | { readonly resultado: 'ok'; readonly valor: T }
  | { readonly resultado: 'negado'; readonly detail: string };

/**
 * Trata `AUTORIZACAO_NEGADA` como resultado de negócio (`negado`), não como exceção.
 * Qualquer outro erro continua propagando como `AppError`.
 */
export function resultadoNegocio<T>(): OperatorFunction<T, ResultadoNegocio<T>> {
  return (origem: Observable<T>) =>
    origem.pipe(
      map((valor): ResultadoNegocio<T> => ({ resultado: 'ok', valor })),
      catchError((erro: unknown) =>
        isAppError(erro) && erro.code === 'AUTORIZACAO_NEGADA'
          ? of<ResultadoNegocio<T>>({ resultado: 'negado', detail: erro.detail })
          : throwError(() => erro),
      ),
    );
}
