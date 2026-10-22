import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { WishlistService } from '../../core/wishlist.service';

/**
 * Ingreso del cliente.
 *
 * El formulario viene precargado con las credenciales de demostracion a
 * proposito: en la sustentacion se entra con un clic.
 */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly deseos = inject(WishlistService);
  private readonly router = inject(Router);
  private readonly ruta = inject(ActivatedRoute);

  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly sesionVencida = this.ruta.snapshot.queryParamMap.get('vencido') === '1';

  readonly formulario = inject(FormBuilder).nonNullable.group({
    email: ['cliente@carvajal.com', [Validators.required, Validators.email]],
    password: ['Carvajal2026*', [Validators.required]],
  });

  get email() {
    return this.formulario.controls.email;
  }

  get password() {
    return this.formulario.controls.password;
  }

  enviar(): void {
    if (this.formulario.invalid || this.cargando()) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    const { email, password } = this.formulario.getRawValue();

    this.auth.login(email, password).subscribe({
      next: () => {
        // Precargamos la lista para que el contador de la cabecera salga al tiro
        this.deseos.cargar().subscribe({
          next: () => this.continuar(),
          error: () => this.continuar(),
        });
      },
      error: (respuesta: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(
          respuesta.error?.mensaje ?? 'No pudimos ingresar. Revisa el correo y la contrasena.',
        );
      },
    });
  }

  private continuar(): void {
    const destino = this.ruta.snapshot.queryParamMap.get('volverA') ?? '/deseos';
    void this.router.navigateByUrl(destino);
  }
}
