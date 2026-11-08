import { Routes } from '@angular/router';

export const AUTORIZACOES_ROUTES: Routes = [
  {
    path: '',
    title: 'Autorizações · Autorizador',
    loadComponent: () => import('./pages/autorizacoes-page').then((m) => m.AutorizacoesPage),
  },
];
