import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth/auth.guard';
import { Shell } from './layout/shell/shell';
import { environment } from '../environments/environment';

/** A galeria do design system só existe fora de produção (o build de produção troca `environment`). */
const rotasDesenvolvimento: Routes = environment.production
  ? []
  : [
      {
        path: '_ds',
        title: 'Design system · Autorizador',
        loadComponent: () =>
          import('./features/design-system/design-system-page').then((m) => m.DesignSystemPage),
      },
    ];

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadChildren: () => import('./features/login/login.routes').then((m) => m.LOGIN_ROUTES),
  },
  {
    path: '',
    component: Shell,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'painel' },
      {
        path: 'painel',
        canMatch: [authGuard],
        loadChildren: () => import('./features/painel/painel.routes').then((m) => m.PAINEL_ROUTES),
      },
      {
        path: 'procedimentos',
        canMatch: [authGuard],
        loadChildren: () =>
          import('./features/procedimentos/procedimentos.routes').then(
            (m) => m.PROCEDIMENTOS_ROUTES,
          ),
      },
      {
        path: 'solicitar',
        canMatch: [authGuard],
        loadChildren: () =>
          import('./features/solicitar/solicitar.routes').then((m) => m.SOLICITAR_ROUTES),
      },
      {
        path: 'autorizacoes',
        canMatch: [authGuard],
        loadChildren: () =>
          import('./features/autorizacoes/autorizacoes.routes').then((m) => m.AUTORIZACOES_ROUTES),
      },
      ...rotasDesenvolvimento,
      {
        path: '**',
        title: 'Página não encontrada · Autorizador',
        loadComponent: () =>
          import('./features/nao-encontrada/nao-encontrada-page').then((m) => m.NaoEncontradaPage),
      },
    ],
  },
];
