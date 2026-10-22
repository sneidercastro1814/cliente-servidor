import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { CatalogService } from '../../core/catalog.service';
import { CopPipe } from '../../core/cop.pipe';
import { Categoria, PaginaRespuesta, Producto } from '../../core/models';
import { WishlistService } from '../../core/wishlist.service';

const TAMANO_PAGINA = 12;

type NivelStock = 'agotado' | 'bajo' | 'suficiente';

/**
 * Requisito funcional 1: catalogo de productos indicando las cantidades
 * existentes, con busqueda, filtro por categoria y paginacion.
 */
@Component({
  selector: 'app-catalogo',
  imports: [CopPipe],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.scss',
})
export class CatalogoComponent {
  private readonly catalogo = inject(CatalogService);
  private readonly deseos = inject(WishlistService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  private readonly terminoEscrito = new Subject<string>();

  readonly paginaActual = signal<PaginaRespuesta<Producto> | null>(null);
  readonly categorias = signal<Categoria[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly aviso = signal<string | null>(null);
  readonly agregandoId = signal<number | null>(null);

  readonly termino = signal('');
  readonly categoriaId = signal<number | null>(null);
  readonly soloConStock = signal(false);
  readonly numeroPagina = signal(0);

  readonly productos = computed(() => this.paginaActual()?.contenido ?? []);
  readonly autenticado = this.auth.autenticado;

  /** Ids que ya estan en la lista, para no ofrecer agregarlos dos veces. */
  readonly idsEnLista = computed(
    () => new Set((this.deseos.lista()?.items ?? []).map((item) => item.producto.id)),
  );

  constructor() {
    this.terminoEscrito
      .pipe(debounceTime(350), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((valor) => {
        this.termino.set(valor);
        this.numeroPagina.set(0);
        this.cargar();
      });

    this.catalogo.categorias().subscribe({
      next: (lista) => this.categorias.set(lista),
      error: () => this.categorias.set([]),
    });

    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.catalogo
      .productos({
        pagina: this.numeroPagina(),
        tamano: TAMANO_PAGINA,
        q: this.termino(),
        categoriaId: this.categoriaId(),
        soloConStock: this.soloConStock(),
      })
      .subscribe({
        next: (pagina) => {
          this.paginaActual.set(pagina);
          this.cargando.set(false);
        },
        error: (respuesta: HttpErrorResponse) => {
          this.cargando.set(false);
          this.error.set(
            respuesta.status === 0
              ? 'No pudimos conectarnos con el servidor. Verifica que la API este arriba en el puerto 8080.'
              : (respuesta.error?.mensaje ?? 'No pudimos cargar el catalogo.'),
          );
        },
      });
  }

  buscar(valor: string): void {
    this.terminoEscrito.next(valor);
  }

  cambiarCategoria(valor: string): void {
    this.categoriaId.set(valor ? Number(valor) : null);
    this.numeroPagina.set(0);
    this.cargar();
  }

  alternarSoloConStock(activo: boolean): void {
    this.soloConStock.set(activo);
    this.numeroPagina.set(0);
    this.cargar();
  }

  irAPagina(numero: number): void {
    this.numeroPagina.set(numero);
    this.cargar();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  nivelStock(producto: Producto): NivelStock {
    if (producto.stock === 0) {
      return 'agotado';
    }
    return producto.stock <= 5 ? 'bajo' : 'suficiente';
  }

  yaEstaEnLista(producto: Producto): boolean {
    return this.idsEnLista().has(producto.id);
  }

  /**
   * Agrega el producto a la lista de deseos.
   *
   * Se permite incluso si esta agotado: es una lista de deseos, y el aviso de
   * stock lo da el servidor al consultar la lista.
   */
  agregar(producto: Producto): void {
    if (!this.autenticado()) {
      void this.router.navigate(['/login'], { queryParams: { volverA: '/catalogo' } });
      return;
    }

    this.agregandoId.set(producto.id);
    this.aviso.set(null);
    this.error.set(null);

    this.deseos.agregar({ productoId: producto.id, cantidadDeseada: 1 }).subscribe({
      next: () => {
        this.agregandoId.set(null);
        this.aviso.set(`"${producto.nombre}" quedo en tu lista de deseos.`);
        this.deseos.cargar().subscribe({ error: () => undefined });
      },
      error: (respuesta: HttpErrorResponse) => {
        this.agregandoId.set(null);
        this.error.set(respuesta.error?.mensaje ?? 'No pudimos agregar el producto.');
      },
    });
  }
}
