package com.carvajal.wishlist.wishlist.domain;

/**
 * Estado de disponibilidad de un deseo frente al stock real del producto.
 *
 * <p>Aqui vive el requisito funcional 3 de la prueba: "si cuando el cliente
 * consulte su lista, alguno de sus deseos ya no se encuentra en stock, debe
 * notificarle".</p>
 *
 * <p>La regla se expresa en un unico punto para que sea facil de probar y de
 * sustentar. El calculo se hace del lado del servidor, en la capa de servicio,
 * porque la prueba exige que la logica de negocio viva en la API REST y no en
 * el cliente Angular.</p>
 */
public enum EstadoStock {

    /** El stock alcanza para la cantidad que el cliente desea. */
    DISPONIBLE,

    /** Queda stock, pero menos del que el cliente pidio. */
    STOCK_INSUFICIENTE,

    /** El producto se quedo en cero: es el caso que obliga a notificar. */
    SIN_STOCK;

    /**
     * Evalua el estado de un deseo.
     *
     * @param stockDisponible existencias actuales del producto
     * @param cantidadDeseada unidades que el cliente quiere
     */
    public static EstadoStock evaluar(int stockDisponible, int cantidadDeseada) {
        if (stockDisponible <= 0) {
            return SIN_STOCK;
        }
        if (stockDisponible < cantidadDeseada) {
            return STOCK_INSUFICIENTE;
        }
        return DISPONIBLE;
    }

    /** @return true si este estado debe generar una notificacion al cliente. */
    public boolean requiereNotificacion() {
        return this != DISPONIBLE;
    }

    /** Mensaje listo para mostrar en el cliente. */
    public String mensaje() {
        return switch (this) {
            case DISPONIBLE -> "Disponible";
            case STOCK_INSUFICIENTE -> "Quedan menos unidades de las que deseas";
            case SIN_STOCK -> "Este producto ya no se encuentra en stock";
        };
    }
}
