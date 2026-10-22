import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Categoria, PaginaRespuesta, Producto } from './models';

export interface FiltrosCatalogo {
  pagina: number;
  tamano: number;
  q?: string;
  categoriaId?: number | null;
  soloConStock?: boolean;
}

/** Acceso al catalogo de productos (endpoints publicos). */
@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly http = inject(HttpClient);

  productos(filtros: FiltrosCatalogo): Observable<PaginaRespuesta<Producto>> {
    let params = new HttpParams()
      .set('page', filtros.pagina)
      .set('size', filtros.tamano);

    const texto = filtros.q?.trim();
    if (texto) {
      params = params.set('q', texto);
    }
    if (filtros.categoriaId) {
      params = params.set('categoriaId', filtros.categoriaId);
    }
    if (filtros.soloConStock) {
      params = params.set('soloConStock', true);
    }

    return this.http.get<PaginaRespuesta<Producto>>('/api/v1/products', { params });
  }

  categorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>('/api/v1/categories');
  }
}
