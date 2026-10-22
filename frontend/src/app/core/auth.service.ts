import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { LoginRespuesta, Usuario } from './models';

const CLAVE_TOKEN = 'carvajal.wishlist.token';
const CLAVE_USUARIO = 'carvajal.wishlist.usuario';

/**
 * Sesion del cliente.
 *
 * El token se guarda en localStorage para sobrevivir a un refresco de pagina.
 * Toda lectura va protegida: en modo privado o con el almacenamiento
 * bloqueado, el acceso puede lanzar excepcion.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly tokenActual = signal<string | null>(leer(CLAVE_TOKEN));
  private readonly usuarioActual = signal<Usuario | null>(leerUsuario());

  readonly usuario = this.usuarioActual.asReadonly();
  readonly autenticado = computed(() => this.tokenActual() !== null);

  token(): string | null {
    return this.tokenActual();
  }

  login(email: string, password: string): Observable<LoginRespuesta> {
    return this.http
      .post<LoginRespuesta>('/api/v1/auth/login', { email, password })
      .pipe(tap((respuesta) => this.guardarSesion(respuesta)));
  }

  cerrarSesion(): void {
    this.tokenActual.set(null);
    this.usuarioActual.set(null);
    borrar(CLAVE_TOKEN);
    borrar(CLAVE_USUARIO);
  }

  private guardarSesion(respuesta: LoginRespuesta): void {
    this.tokenActual.set(respuesta.token);
    this.usuarioActual.set(respuesta.usuario);
    escribir(CLAVE_TOKEN, respuesta.token);
    escribir(CLAVE_USUARIO, JSON.stringify(respuesta.usuario));
  }
}

function leer(clave: string): string | null {
  try {
    return localStorage.getItem(clave);
  } catch {
    return null;
  }
}

function escribir(clave: string, valor: string): void {
  try {
    localStorage.setItem(clave, valor);
  } catch {
    // sin almacenamiento la sesion solo dura lo que dure la pestana
  }
}

function borrar(clave: string): void {
  try {
    localStorage.removeItem(clave);
  } catch {
    // nada que hacer
  }
}

function leerUsuario(): Usuario | null {
  const crudo = leer(CLAVE_USUARIO);
  if (!crudo) {
    return null;
  }
  try {
    return JSON.parse(crudo) as Usuario;
  } catch {
    return null;
  }
}
