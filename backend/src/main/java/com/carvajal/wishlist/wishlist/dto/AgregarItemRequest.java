package com.carvajal.wishlist.wishlist.dto;

import com.carvajal.wishlist.wishlist.domain.Prioridad;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo de {@code POST /api/v1/wishlist/items}. */
public record AgregarItemRequest(

        @NotNull(message = "el id del producto es obligatorio")
        Long productoId,

        @NotNull(message = "la cantidad deseada es obligatoria")
        @Min(value = 1, message = "la cantidad deseada debe ser al menos 1")
        @Max(value = 999, message = "la cantidad deseada no puede superar 999")
        Integer cantidadDeseada,

        Prioridad prioridad,

        @Size(max = 300, message = "la nota no puede superar 300 caracteres")
        String nota
) {

    /** Prioridad por defecto si el cliente no la manda. */
    public Prioridad prioridadOrDefault() {
        return prioridad == null ? Prioridad.MEDIA : prioridad;
    }
}
