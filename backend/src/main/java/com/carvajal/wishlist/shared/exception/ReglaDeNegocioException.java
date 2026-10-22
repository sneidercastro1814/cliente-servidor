package com.carvajal.wishlist.shared.exception;

/**
 * Se violo una regla de negocio (por ejemplo, agregar dos veces el mismo
 * producto a la lista). El manejador global la traduce a HTTP 409 Conflict.
 */
public class ReglaDeNegocioException extends RuntimeException {

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
