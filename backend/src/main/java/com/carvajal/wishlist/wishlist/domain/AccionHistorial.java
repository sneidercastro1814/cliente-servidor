package com.carvajal.wishlist.wishlist.domain;

/**
 * Tipo de evento registrado en el historico de la lista de deseos.
 *
 * <p>Soporta el requisito funcional 4: "debe almacenarse el historico de
 * registros que hayan llegado a la lista de deseos". Modelamos eventos y no
 * un borrado logico, para no perder los cambios intermedios de cantidad.</p>
 */
public enum AccionHistorial {
    AGREGADO,
    ACTUALIZADO,
    ELIMINADO
}
