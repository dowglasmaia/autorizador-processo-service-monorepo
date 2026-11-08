import { inject } from '@angular/core';
import { CanActivateFn, CanMatchFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { ROTA_PADRAO } from './retorno-seguro';

/** Exige sessão; sem ela vai a `/login` preservando o destino em `retorno`. */
export const authGuard: CanMatchFn = (_route, segments) => {
  if (inject(AuthService).autenticado()) return true;
  const router = inject(Router);
  const destino =
    router.getCurrentNavigation()?.extractedUrl.toString() ??
    `/${segments.map((s) => s.path).join('/')}`;
  return router.createUrlTree(['/login'], { queryParams: { retorno: destino } });
};

/** Impede usuário autenticado de ver `/login`. */
export const guestGuard: CanActivateFn = () =>
  inject(AuthService).autenticado() ? inject(Router).createUrlTree([ROTA_PADRAO]) : true;
