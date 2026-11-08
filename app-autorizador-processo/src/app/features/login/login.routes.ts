import { Routes } from '@angular/router';

export const LOGIN_ROUTES: Routes = [
  {
    path: '',
    title: 'Entrar · Autorizador',
    loadComponent: () => import('./pages/login-page').then((m) => m.LoginPage),
  },
];
