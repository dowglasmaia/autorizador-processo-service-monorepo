import { Routes } from '@angular/router';

export const PROCEDIMENTOS_ROUTES: Routes = [
  {
    path: '',
    title: 'Procedimentos · Autorizador',
    loadComponent: () => import('./pages/procedimentos-page').then((m) => m.ProcedimentosPage),
  },
  {
    path: 'novo',
    title: 'Novo procedimento · Autorizador',
    loadComponent: () =>
      import('./pages/procedimento-novo-page').then((m) => m.ProcedimentoNovoPage),
  },
];
