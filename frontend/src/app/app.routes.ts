import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

/**
 * Rutas de la aplicacion. Cada vista se carga de forma diferida
 * (`loadComponent`), asi el paquete inicial solo trae lo necesario para pintar
 * el catalogo.
 */
export const routes: Routes = [
  { path: '', redirectTo: 'catalogo', pathMatch: 'full' },
  {
    path: 'login',
    title: 'Ingresar | Carvajal',
    loadComponent: () => import('./features/login/login').then((m) => m.LoginComponent),
  },
  {
    path: 'catalogo',
    title: 'Catalogo | Carvajal',
    loadComponent: () => import('./features/catalogo/catalogo').then((m) => m.CatalogoComponent),
  },
  {
    path: 'deseos',
    title: 'Mi lista de deseos | Carvajal',
    canActivate: [authGuard],
    loadComponent: () => import('./features/deseos/deseos').then((m) => m.DeseosComponent),
  },
  {
    path: 'historico',
    title: 'Historico | Carvajal',
    canActivate: [authGuard],
    loadComponent: () => import('./features/historico/historico').then((m) => m.HistoricoComponent),
  },
  { path: '**', redirectTo: 'catalogo' },
];
