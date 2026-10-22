import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { CopPipe } from '../../core/cop.pipe';
import { AccionHistorial, EventoHistorial, PaginaRespuesta } from '../../core/models';
import { WishlistService } from '../../core/wishlist.service';

const TAMANO_PAGINA = 15;

/**
 * Requisito funcional 4: historico de todos los registros que han llegado a la
 * lista de deseos.
 *
 * Los datos de producto que se muestran aqui son el snapshot guardado en el
 * momento del evento, no los actuales: por eso un producto que ya se elimino de
 * la lista sigue apareciendo con el precio que tenia ese dia.
 */
@Component({
  selector: 'app-historico',
  imports: [CopPipe],
  templateUrl: './historico.html',
  styleUrl: './historico.scss',
})
export class HistoricoComponent {
  private readonly deseos = inject(WishlistService);

  readonly pagina = signal<PaginaRespuesta<EventoHistorial> | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly numeroPagina = signal(0);
  readonly accion = signal<AccionHistorial | null>(null);

  readonly eventos = computed(() => this.pagina()?.contenido ?? []);

  readonly filtros: { valor: AccionHistorial | null; etiqueta: string }[] = [
    { valor: null, etiqueta: 'Todos' },
    { valor: 'AGREGADO', etiqueta: 'Agregados' },
    { valor: 'ACTUALIZADO', etiqueta: 'Actualizados' },
    { valor: 'ELIMINADO', etiqueta: 'Eliminados' },
  ];

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.deseos.historial(this.numeroPagina(), TAMANO_PAGINA, this.accion()).subscribe({
      next: (pagina) => {
        this.pagina.set(pagina);
        this.cargando.set(false);
      },
      error: (respuesta: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(
          respuesta.status === 404
            ? 'Todavia no hay historico: tu lista de deseos aun no tiene movimientos.'
            : (respuesta.error?.mensaje ?? 'No pudimos cargar el historico.'),
        );
      },
    });
  }

  filtrar(valor: AccionHistorial | null): void {
    this.accion.set(valor);
    this.numeroPagina.set(0);
    this.cargar();
  }

  irAPagina(numero: number): void {
    this.numeroPagina.set(numero);
    this.cargar();
  }

  claseAccion(accion: AccionHistorial): string {
    switch (accion) {
      case 'AGREGADO':
        return 'distintivo-ok';
      case 'ACTUALIZADO':
        return 'distintivo-neutro';
      default:
        return 'distintivo-peligro';
    }
  }

  fecha(iso: string): string {
    const valor = new Date(iso);
    if (Number.isNaN(valor.getTime())) {
      return iso;
    }
    return valor.toLocaleString('es-CO', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}
