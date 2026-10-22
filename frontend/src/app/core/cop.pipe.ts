import { Pipe, PipeTransform } from '@angular/core';

/**
 * Formatea pesos colombianos.
 *
 * Se usa Intl directamente en vez del pipe `currency` de Angular para no tener
 * que registrar el locale es-CO: menos configuracion y el mismo resultado.
 */
@Pipe({ name: 'cop' })
export class CopPipe implements PipeTransform {
  private readonly formato = new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  });

  transform(valor: number | null | undefined): string {
    return valor === null || valor === undefined ? '' : this.formato.format(valor);
  }
}
