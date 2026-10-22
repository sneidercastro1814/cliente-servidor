import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * Agrega el JWT a cada peticion y reacciona a un token vencido.
 *
 * Es un interceptor funcional, la forma recomendada en Angular moderno: no
 * necesita clase ni provider con token de inyeccion.
 */
export const authInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const token = auth.token();
  const conAuth = token
    ? peticion.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : peticion;

  return siguiente(conAuth).pipe(
    catchError((error: HttpErrorResponse) => {
      // 401 con sesion activa = el token vencio o dejo de ser valido
      if (error.status === 401 && auth.autenticado()) {
        auth.cerrarSesion();
        void router.navigate(['/login'], { queryParams: { vencido: '1' } });
      }
      return throwError(() => error);
    }),
  );
};
