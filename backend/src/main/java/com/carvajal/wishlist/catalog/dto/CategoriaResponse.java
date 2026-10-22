package com.carvajal.wishlist.catalog.dto;

import com.carvajal.wishlist.catalog.domain.Category;

public record CategoriaResponse(Long id, String nombre, String slug, String descripcion) {

    public static CategoriaResponse de(Category c) {
        if (c == null) {
            return null;
        }
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getSlug(), c.getDescripcion());
    }
}
