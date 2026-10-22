package com.carvajal.wishlist.catalog.dto;

import com.carvajal.wishlist.catalog.domain.Product;

import java.math.BigDecimal;

/**
 * Producto tal como lo ve el cliente.
 *
 * <p>Incluye {@code stock} porque el primer requisito funcional pide mostrar el
 * catalogo "indicando las cantidades existentes", y {@code disponible} como
 * campo derivado para que el front no tenga que repetir la regla.</p>
 */
public record ProductoResponse(
        Long id,
        String sku,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        boolean disponible,
        String marca,
        String imagenUrl,
        CategoriaResponse categoria
) {

    public static ProductoResponse de(Product p) {
        return new ProductoResponse(
                p.getId(),
                p.getSku(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.stockSeguro(),
                p.tieneStock(),
                p.getMarca(),
                p.getImagenUrl(),
                CategoriaResponse.de(p.getCategory())
        );
    }
}
