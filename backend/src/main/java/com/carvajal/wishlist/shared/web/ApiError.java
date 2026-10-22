package com.carvajal.wishlist.shared.web;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato unico de error de la API. Tener una sola forma de error le simplifica
 * la vida al cliente Angular, que puede manejar todos los fallos igual.
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String path,
        List<String> detalles
) {

    public static ApiError de(int status, String error, String mensaje, String path) {
        return new ApiError(LocalDateTime.now(), status, error, mensaje, path, null);
    }

    public static ApiError conDetalles(int status, String error, String mensaje, String path, List<String> detalles) {
        return new ApiError(LocalDateTime.now(), status, error, mensaje, path, detalles);
    }
}
