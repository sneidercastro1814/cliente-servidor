package com.carvajal.wishlist.wishlist.dto;

import com.carvajal.wishlist.wishlist.domain.AccionHistorial;
import com.carvajal.wishlist.wishlist.domain.WishlistItemHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento del historico. Los campos de producto vienen del snapshot guardado en
 * el momento del evento, no del producto actual: por eso el historico sigue
 * siendo fiel aunque el producto haya cambiado de precio o de nombre despues.
 */
public record HistorialResponse(
        Long id,
        AccionHistorial accion,
        Long productoId,
        String productoNombre,
        BigDecimal productoPrecio,
        Integer cantidadDeseada,
        Integer stockAlMomento,
        LocalDateTime fecha
) {

    public static HistorialResponse de(WishlistItemHistory h) {
        return new HistorialResponse(
                h.getId(),
                h.getAccion(),
                h.getProduct().getId(),
                h.getProductoNombre(),
                h.getProductoPrecio(),
                h.getCantidadDeseada(),
                h.getStockAlMomento(),
                h.getOccurredAt()
        );
    }
}
