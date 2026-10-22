import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';
import { WishlistService } from './core/wishlist.service';

/**
 * Cascaron de la aplicacion: cabecera, navegacion y salida del router.
 *
 * El contador de alertas de la cabecera lee la signal del servicio de lista de
 * deseos, asi que se mantiene al dia sin pedirle nada mas al servidor.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly auth = inject(AuthService);
  private readonly deseos = inject(WishlistService);
  private readonly router = inject(Router);

  readonly usuario = this.auth.usuario;
  readonly autenticado = this.auth.autenticado;
  readonly totalDeseos = this.deseos.totalDeseos;
  readonly totalAlertas = this.deseos.totalAlertas;

  constructor() {
    // Si quedo sesion de una visita anterior, precargamos la lista para que el
    // contador de la cabecera aparezca de una.
    if (this.auth.autenticado()) {
      this.deseos.cargar().subscribe({ error: () => undefined });
    }
  }

  salir(): void {
    this.auth.cerrarSesion();
    this.deseos.limpiar();
    void this.router.navigate(['/catalogo']);
  }
}
