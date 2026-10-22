package com.carvajal.wishlist.wishlist;

import com.carvajal.wishlist.wishlist.domain.EstadoStock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba unitaria de la regla de negocio del requisito funcional 3.
 *
 * <p>No levanta Spring ni toca base de datos: la regla esta aislada en un enum,
 * asi que se puede probar en milisegundos y cubriendo todos los casos frontera.</p>
 */
@DisplayName("Regla de disponibilidad de stock")
class EstadoStockTest {

    @ParameterizedTest(name = "stock={0}, deseado={1} -> {2}")
    @CsvSource({
            // stock, cantidadDeseada, esperado
            "10, 1,  DISPONIBLE",
            "10, 10, DISPONIBLE",          // alcanza justo
            "10, 11, STOCK_INSUFICIENTE",  // falta una unidad
            "1,  5,  STOCK_INSUFICIENTE",
            "0,  1,  SIN_STOCK",           // el caso que obliga a notificar
            "0,  99, SIN_STOCK"
    })
    void evalua_el_estado_segun_el_stock(int stock, int cantidadDeseada, EstadoStock esperado) {
        assertThat(EstadoStock.evaluar(stock, cantidadDeseada)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("un stock negativo se trata como agotado y no revienta")
    void stock_negativo_se_trata_como_agotado() {
        assertThat(EstadoStock.evaluar(-3, 1)).isEqualTo(EstadoStock.SIN_STOCK);
    }

    @Test
    @DisplayName("solo los estados problematicos generan notificacion")
    void solo_los_estados_problematicos_notifican() {
        assertThat(EstadoStock.DISPONIBLE.requiereNotificacion()).isFalse();
        assertThat(EstadoStock.STOCK_INSUFICIENTE.requiereNotificacion()).isTrue();
        assertThat(EstadoStock.SIN_STOCK.requiereNotificacion()).isTrue();
    }

    @Test
    @DisplayName("todos los estados traen un mensaje para mostrar al cliente")
    void todos_los_estados_tienen_mensaje() {
        for (EstadoStock estado : EstadoStock.values()) {
            assertThat(estado.mensaje()).isNotBlank();
        }
    }
}
