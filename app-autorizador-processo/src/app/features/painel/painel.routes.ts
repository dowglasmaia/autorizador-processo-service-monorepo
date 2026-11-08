import { Routes } from '@angular/router';

export const PAINEL_ROUTES: Routes = [
  {
    path: '',
    title: 'Painel · Autorizador',
    loadComponent: () => import('./pages/painel-page').then((m) => m.PainelPage),
  },
];
