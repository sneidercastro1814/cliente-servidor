import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { CopPipe } from '../../core/cop.pipe';
import { DeseoItem, EstadoStock, Prioridad } from '../../core/models';
import { WishlistService } from '../../core/wishlist.service';

/**
 * Lista de deseos del cliente.
 *
 * Cubre el requisito 2 (listar, agregar desde el catalogo, actualizar y
 * eliminar) y el requisito 3 (mostrar la notificacion cuando un deseo se queda
 * sin stock). Importante: el estado de stock NO se calcula aqui; llega
 * resuelto desde la API, porque la prueba exige que la logica de negocio viva
 * en el servidor.
 */
@Component({
  selector: 'app-deseos',
  imports: [CopPipe],
  templateUrl: './deseos.html',
  styleUrl: './deseos.scss',
})
export class DeseosComponent {
  private readonly deseos = inject(WishlistService);

  readonly lista = this.deseos.lista;
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly aviso = signal<string | null>(null);

  readonly editandoId = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly eliminandoId = signal<number | null>(null);

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.deseos.cargar().subscribe({
      next: () => this.cargando.set(false),
      error: (respuesta: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(
          respuesta.status === 0
            ? 'No pudimos conectarnos con el servidor. Verifica que la API este arriba en el puerto 8080.'
            : (respuesta.error?.mensaje ?? 'No pudimos cargar tu lista de deseos.'),
        );
      },
    });
  }

  abrirEdicion(item: DeseoItem): void {
    this.aviso.set(null);
    this.editandoId.set(item.id);
  }

  cancelarEdicion(): void {
    this.editandoId.set(null);
  }

  guardar(item: DeseoItem, cantidad: string, prioridad: string, nota: string): void {
    const cantidadNumero = Number(cantidad);
    if (!Number.isInteger(cantidadNumero) || cantidadNumero < 1 || cantidadNumero > 999) {
      this.error.set('La cantidad debe ser un numero entero entre 1 y 999.');
      return;
    }

    this.guardando.set(true);
    this.error.set(null);

    this.deseos
      .actualizar(item.id, {
        cantidadDeseada: cantidadNumero,
        prioridad: prioridad as Prioridad,
        nota: nota.trim() ? nota.trim() : null,
      })
      .subscribe({
        next: () => {
          this.guardando.set(false);
          this.editandoId.set(null);
          this.aviso.set(`Actualizamos "${item.producto.nombre}".`);
          this.deseos.cargar().subscribe({ error: () => undefined });
        },
        error: (respuesta: HttpErrorResponse) => {
          this.guardando.set(false);
          this.error.set(respuesta.error?.mensaje ?? 'No pudimos actualizar el deseo.');
        },
      });
  }

  eliminar(item: DeseoItem): void {
    this.eliminandoId.set(item.id);
    this.error.set(null);
    this.aviso.set(null);

    this.deseos.eliminar(item.id).subscribe({
      next: () => {
        this.eliminandoId.set(null);
        this.aviso.set(
          `Quitamos "${item.producto.nombre}" de tu lista. El movimiento queda en el historico.`,
        );
        this.deseos.cargar().subscribe({ error: () => undefined });
      },
      error: (respuesta: HttpErrorResponse) => {
        this.eliminandoId.set(null);
        this.error.set(respuesta.error?.mensaje ?? 'No pudimos eliminar el deseo.');
      },
    });
  }

  claseEstado(estado: EstadoStock): string {
    switch (estado) {
      case 'DISPONIBLE':
        return 'distintivo-ok';
      case 'STOCK_INSUFICIENTE':
        return 'distintivo-advertencia';
      default:
        return 'distintivo-peligro';
    }
  }

  etiquetaEstado(estado: EstadoStock): string {
    switch (estado) {
      case 'DISPONIBLE':
        return 'Disponible';
      case 'STOCK_INSUFICIENTE':
        return 'Stock insuficiente';
      default:
        return 'Sin stock';
    }
  }

  clasePrioridad(prioridad: Prioridad): string {
    switch (prioridad) {
      case 'ALTA':
        return 'distintivo-neutro';
      case 'MEDIA':
        return 'distintivo-gris';
      default:
        return 'distintivo-gris';
    }
  }
}
