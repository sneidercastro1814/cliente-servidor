import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ListaDeseos, PaginaRespuesta, EventoHistorial } from './models';
import { WishlistService } from './wishlist.service';

describe('WishlistService', () => {
  let servicio: WishlistService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(WishlistService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('expone el conteo de alertas que calculo el servidor', () => {
    const respuesta: ListaDeseos = {
      id: 1,
      nombre: 'Mi lista de deseos',
      totalItems: 2,
      itemsConNotificacion: 1,
      notificaciones: ['Morral escolar: Este producto ya no se encuentra en stock'],
      items: [],
    };

    servicio.cargar().subscribe();
    http.expectOne('/api/v1/wishlist').flush(respuesta);

    expect(servicio.totalDeseos()).toBe(2);
    expect(servicio.totalAlertas()).toBe(1);
    expect(servicio.lista()?.notificaciones.length).toBe(1);
  });

  it('manda el filtro de accion y el tamano de pagina al pedir el historico', () => {
    const vacio: PaginaRespuesta<EventoHistorial> = {
      contenido: [],
      pagina: 0,
      tamano: 15,
      totalElementos: 0,
      totalPaginas: 0,
      primera: true,
      ultima: true,
    };

    servicio.historial(0, 15, 'ELIMINADO').subscribe();

    const peticion = http.expectOne((r) => r.url === '/api/v1/wishlist/history');
    expect(peticion.request.params.get('accion')).toBe('ELIMINADO');
    expect(peticion.request.params.get('size')).toBe('15');
    peticion.flush(vacio);
  });

  it('usa DELETE para quitar un deseo', () => {
    servicio.eliminar(50).subscribe();

    const peticion = http.expectOne('/api/v1/wishlist/items/50');
    expect(peticion.request.method).toBe('DELETE');
    peticion.flush(null);
  });

  it('limpiar deja la lista en nulo al cerrar sesion', () => {
    servicio.cargar().subscribe();
    http.expectOne('/api/v1/wishlist').flush({
      id: 1,
      nombre: 'Mi lista de deseos',
      totalItems: 1,
      itemsConNotificacion: 0,
      notificaciones: [],
      items: [],
    } satisfies ListaDeseos);

    expect(servicio.totalDeseos()).toBe(1);

    servicio.limpiar();

    expect(servicio.lista()).toBeNull();
    expect(servicio.totalAlertas()).toBe(0);
  });
});
