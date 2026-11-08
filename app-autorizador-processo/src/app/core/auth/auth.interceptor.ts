import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { isAppError } from '../errors/app-error';
import { API_BASE_URL } from '../http/api-base-url.token';
import { AuthService } from './auth.service';

/** `true` se `url` está sob `apiBaseUrl` (relativa ou absoluta); qualquer outro host é recusado. */
export function urlDaApi(url: string, apiBaseUrl: string): boolean {
  const base = apiBaseUrl.replace(/\/+$/, '');
  const sob = (prefixo: string) => url === prefixo || url.startsWith(`${prefixo}/`);
  if (base.startsWith('/')) {
    // `//host/...` é URL absoluta sem esquema, nunca "relativa à API".
    if (url.startsWith('//')) return false;
    return sob(base) || (typeof location !== 'undefined' && sob(`${location.origin}${base}`));
  }
  return sob(base);
}

/**
 * Anexa `Authorization: Bearer` somente a URLs da API e, em 401 fora do login, encerra a sessão
 * e redireciona. Registrado antes do `errorInterceptor`, recebe `AppError` na volta.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const daApi = urlDaApi(req.url, inject(API_BASE_URL));
  const token = auth.token();
  const requisicao =
    daApi && token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(requisicao).pipe(
    catchError((erro: unknown) => {
      if (
        daApi &&
        isAppError(erro) &&
        erro.status === 401 &&
        erro.code !== 'CREDENCIAIS_INVALIDAS'
      ) {
        const retorno = router.url.startsWith('/login') ? undefined : router.url;
        auth.sair({ retorno, expirada: token !== null });
      }
      return throwError(() => erro);
    }),
  );
};
