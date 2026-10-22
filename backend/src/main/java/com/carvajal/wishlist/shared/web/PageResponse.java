package com.carvajal.wishlist.shared.web;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Envoltura propia para respuestas paginadas.
 *
 * <p>Se usa en lugar de serializar directamente el {@code Page} de Spring Data
 * porque la estructura JSON de {@code Page} no es un contrato estable entre
 * versiones de Spring. Exponer un DTO propio nos deja el contrato de la API
 * bajo nuestro control.</p>
 */
public record PageResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        boolean primera,
        boolean ultima
) {

    public static <E, T> PageResponse<T> de(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
