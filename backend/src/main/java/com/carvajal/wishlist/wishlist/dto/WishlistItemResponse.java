package com.carvajal.wishlist.wishlist.dto;

import com.carvajal.wishlist.catalog.dto.ProductoResponse;
import com.carvajal.wishlist.wishlist.domain.EstadoStock;
import com.carvajal.wishlist.wishlist.domain.Prioridad;
import com.carvajal.wishlist.wishlist.domain.WishlistItem;

import java.time.LocalDateTime;

/**
 * Un deseo con su evaluacion de stock ya resuelta por el servidor.
 *
 * <p>{@code estadoStock}, {@code mensajeStock} y {@code requiereNotificacion}
 * son el requisito funcional 3 materializado en el contrato de la API: el
 * cliente Angular solo pinta, no decide.</p>
 */
public record WishlistItemResponse(
        Long id,
        ProductoResponse producto,
        Integer cantidadDeseada,
        Prioridad prioridad,
        String nota,
        EstadoStock estadoStock,
        String mensajeStock,
        boolean requiereNotificacion,
        LocalDateTime agregadoEn,
        LocalDateTime actualizadoEn
) {

    public static WishlistItemResponse de(WishlistItem item) {
        EstadoStock estado = item.estadoStock();
        return new WishlistItemResponse(
                item.getId(),
                ProductoResponse.de(item.getProduct()),
                item.getCantidadDeseada(),
                item.getPrioridad(),
                item.getNota(),
                estado,
                estado.mensaje(),
                estado.requiereNotificacion(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
