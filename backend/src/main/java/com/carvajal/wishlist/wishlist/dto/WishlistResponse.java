package com.carvajal.wishlist.wishlist.dto;

import java.util.List;

/**
 * Respuesta de {@code GET /api/v1/wishlist}.
 *
 * <p>Ademas de los items trae un resumen de notificaciones listo para que el
 * front muestre el aviso del requisito funcional 3 sin recorrer la lista.</p>
 */
public record WishlistResponse(
        Long id,
        String nombre,
        int totalItems,
        int itemsConNotificacion,
        List<String> notificaciones,
        List<WishlistItemResponse> items
) {
}
