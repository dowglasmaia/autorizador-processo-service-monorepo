import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError, TimeoutError } from 'rxjs';
import {
  AppError,
  MENSAGEM_REDE,
  MENSAGEM_SERVIDOR,
  isAppError,
  mapProblem,
} from '../errors/app-error';
import type { ProblemDetail } from '../../shared/api';

function eProblema(corpo: unknown): corpo is Partial<ProblemDetail> {
  return typeof corpo === 'object' && corpo !== null && !Array.isArray(corpo);
}

/** Converte qualquer falha HTTP em `AppError`, sem expor detalhes técnicos. */
export function paraAppError(erro: unknown): AppError {
  if (isAppError(erro)) return erro;
  if (erro instanceof TimeoutError) return { kind: 'rede', detail: MENSAGEM_REDE };
  if (erro instanceof HttpErrorResponse) {
    if (erro.status === 0) return { kind: 'rede', detail: MENSAGEM_REDE, status: 0 };
    // Resposta 2xx com corpo ilegível (JSON inválido) ou corpo que não é problem+json.
    if (erro.ok || !eProblema(erro.error)) {
      return { kind: 'servidor', detail: MENSAGEM_SERVIDOR, status: erro.status };
    }
    return mapProblem(erro.error, erro.status);
  }
  return { kind: 'servidor', detail: MENSAGEM_SERVIDOR };
}

export const errorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(catchError((erro: unknown) => throwError(() => paraAppError(erro))));
