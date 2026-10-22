package com.carvajal.wishlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Punto de entrada de la API REST de Lista de Deseos.
 *
 * <p>Arquitectura: cliente-servidor. Este modulo es el servidor (API REST) y el
 * cliente es la SPA de Angular que vive en /frontend.</p>
 *
 * <p>El proyecto esta organizado como monolito modular: cada paquete de primer
 * nivel ({@code catalog}, {@code wishlist}, {@code auth}) es un modulo de
 * negocio independiente con su propio dominio, repositorio, servicio y
 * controlador. Esa frontera es la que permitiria extraer cada modulo a un
 * microservicio sin reescribir la logica.</p>
 */
@SpringBootApplication
@EnableJpaAuditing
public class WishlistApplication {

    public static void main(String[] args) {
        SpringApplication.run(WishlistApplication.class, args);
    }
}
