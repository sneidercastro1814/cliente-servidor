package com.carvajal.wishlist.shared.exception;

/**
 * El recurso solicitado no existe (o no le pertenece al usuario autenticado).
 * El manejador global la traduce a HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public static RecursoNoEncontradoException de(String recurso, Object id) {
        return new RecursoNoEncontradoException("No se encontro %s con identificador %s".formatted(recurso, id));
    }
}
