import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import {
  AccionHistorial,
  ActualizarDeseoPeticion,
  AgregarDeseoPeticion,
  DeseoItem,
  EventoHistorial,
  ListaDeseos,
  PaginaRespuesta,
} from './models';

/**
 * Lista de deseos del cliente.
 *
 * Guarda la ultima lista cargada en una signal para que la cabecera pueda
 * mostrar el contador de alertas sin volver a pedirla al servidor.
 */
@Injectable({ providedIn: 'root' })
export class WishlistService {
  private readonly http = inject(HttpClient);

  private readonly listaActual = signal<ListaDeseos | null>(null);

  readonly lista = this.listaActual.asReadonly();
  readonly totalDeseos = computed(() => this.listaActual()?.totalItems ?? 0);
  readonly totalAlertas = computed(() => this.listaActual()?.itemsConNotificacion ?? 0);

  /** Requisitos 2 (listar) y 3 (notificar): el servidor ya manda las alertas. */
  cargar(): Observable<ListaDeseos> {
    return this.http
      .get<ListaDeseos>('/api/v1/wishlist')
      .pipe(tap((lista) => this.listaActual.set(lista)));
  }

  agregar(peticion: AgregarDeseoPeticion): Observable<DeseoItem> {
    return this.http.post<DeseoItem>('/api/v1/wishlist/items', peticion);
  }

  actualizar(itemId: number, peticion: ActualizarDeseoPeticion): Observable<DeseoItem> {
    return this.http.put<DeseoItem>(`/api/v1/wishlist/items/${itemId}`, peticion);
  }

  eliminar(itemId: number): Observable<void> {
    return this.http.delete<void>(`/api/v1/wishlist/items/${itemId}`);
  }

  /** Requisito 4: historico de todo lo que ha pasado por la lista. */
  historial(
    pagina: number,
    tamano: number,
    accion?: AccionHistorial | null,
  ): Observable<PaginaRespuesta<EventoHistorial>> {
    let params = new HttpParams().set('page', pagina).set('size', tamano);
    if (accion) {
      params = params.set('accion', accion);
    }
    return this.http.get<PaginaRespuesta<EventoHistorial>>('/api/v1/wishlist/history', { params });
  }

  limpiar(): void {
    this.listaActual.set(null);
  }
}
