import { Routes } from '@angular/router';

export const SOLICITAR_ROUTES: Routes = [
  {
    path: '',
    title: 'Solicitar autorização · Autorizador',
    loadComponent: () => import('./pages/solicitar-page').then((m) => m.SolicitarPage),
  },
];
