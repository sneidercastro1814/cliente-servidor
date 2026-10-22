package com.carvajal.wishlist.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Documentacion OpenAPI. El esquema de seguridad declarado aqui es lo que hace
 * aparecer el boton "Authorize" en Swagger UI, para poder pegar el JWT y probar
 * los endpoints de la lista de deseos desde el navegador durante la demo.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Lista de Deseos - Carvajal",
                version = "1.0.0",
                description = """
                        API REST del modulo Lista de Deseos para el e-commerce B2C de Carvajal.

                        Flujo para probarla:
                        1. POST /api/v1/auth/login con cliente@carvajal.com / Carvajal2026*
                        2. Copiar el token y pegarlo en el boton Authorize
                        3. GET /api/v1/wishlist para ver la lista con las alertas de stock
                        """,
                contact = @Contact(name = "Equipo de desarrollo")
        ),
        servers = @Server(url = "/", description = "Servidor actual"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
