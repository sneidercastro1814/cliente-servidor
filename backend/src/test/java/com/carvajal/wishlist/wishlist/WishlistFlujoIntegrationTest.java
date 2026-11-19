package com.carvajal.wishlist.wishlist;

import com.carvajal.wishlist.auth.domain.Role;
import com.carvajal.wishlist.auth.domain.User;
import com.carvajal.wishlist.auth.repository.UserRepository;
import com.carvajal.wishlist.catalog.domain.Category;
import com.carvajal.wishlist.catalog.domain.Product;
import com.carvajal.wishlist.catalog.repository.CategoryRepository;
import com.carvajal.wishlist.catalog.repository.ProductRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integracion del flujo completo de la lista de deseos: requisitos
 * funcionales 2, 3 y 4 de punta a punta, pasando por controlador, servicio,
 * ORM y base de datos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "cliente@carvajal.com")
@DisplayName("Flujo completo de la lista de deseos (integracion)")
class WishlistFlujoIntegrationTest {

    private static final String CLIENTE = "cliente@carvajal.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;

    private Long idMouse;
    private Long idAgotado;

    @BeforeEach
    void prepararDatos() {
        userRepository.save(new User("Laura Gomez", CLIENTE, "$2b$10$hashIrrelevante", Role.CLIENTE));

        Category tecnologia = categoryRepository.save(new Category("Tecnologia", "tecnologia", null));

        idMouse = productRepository.save(new Product("TEC-MOU-001", "Mouse inalambrico",
                new BigDecimal("54900.00"), 10, tecnologia)).getId();
        idAgotado = productRepository.save(new Product("ESC-MOC-001", "Morral escolar",
                new BigDecimal("89900.00"), 0, tecnologia)).getId();
    }

    @Test
    @DisplayName("listar, agregar, actualizar y eliminar, con notificacion de stock e historico")
    void flujo_completo() throws Exception {
        // ---------------------------------------------------------------------
        // 1. La lista arranca vacia (se crea en el primer acceso)
        // ---------------------------------------------------------------------
        mockMvc.perform(get("/api/v1/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.itemsConNotificacion").value(0));

        // ---------------------------------------------------------------------
        // 2. Agregar un producto con stock suficiente
        // ---------------------------------------------------------------------
        String creado = mockMvc.perform(post("/api/v1/wishlist/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productoId": %d, "cantidadDeseada": 2, "prioridad": "MEDIA"}
                                """.formatted(idMouse)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estadoStock").value("DISPONIBLE"))
                .andExpect(jsonPath("$.requiereNotificacion").value(false))
                .andReturn().getResponse().getContentAsString();

        long idItemMouse = ((Number) JsonPath.read(creado, "$.id")).longValue();

        // ---------------------------------------------------------------------
        // 3. El mismo producto dos veces es conflicto, no duplicado
        // ---------------------------------------------------------------------
        mockMvc.perform(post("/api/v1/wishlist/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productoId": %d, "cantidadDeseada": 1}
                                """.formatted(idMouse)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        // ---------------------------------------------------------------------
        // 4. Agregar un producto agotado: se permite, y dispara la notificacion
        // ---------------------------------------------------------------------
        mockMvc.perform(post("/api/v1/wishlist/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productoId": %d, "cantidadDeseada": 1, "prioridad": "ALTA"}
                                """.formatted(idAgotado)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estadoStock").value("SIN_STOCK"))
                .andExpect(jsonPath("$.requiereNotificacion").value(true));

        // ---------------------------------------------------------------------
        // 5. Requisito 3: al consultar, la API avisa del agotado
        // ---------------------------------------------------------------------
        mockMvc.perform(get("/api/v1/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.itemsConNotificacion").value(1))
                .andExpect(jsonPath("$.notificaciones", hasSize(1)))
                .andExpect(jsonPath("$.notificaciones[0]", containsString("Morral escolar")));

        // ---------------------------------------------------------------------
        // 6. Subir la cantidad por encima del stock pasa a STOCK_INSUFICIENTE
        // ---------------------------------------------------------------------
        mockMvc.perform(put("/api/v1/wishlist/items/" + idItemMouse)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cantidadDeseada": 99, "prioridad": "ALTA", "nota": "necesito mas"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadDeseada").value(99))
                .andExpect(jsonPath("$.estadoStock").value("STOCK_INSUFICIENTE"))
                .andExpect(jsonPath("$.nota").value("necesito mas"));

        mockMvc.perform(get("/api/v1/wishlist"))
                .andExpect(jsonPath("$.itemsConNotificacion").value(2));

        // ---------------------------------------------------------------------
        // 7. Eliminar el deseo
        // ---------------------------------------------------------------------
        mockMvc.perform(delete("/api/v1/wishlist/items/" + idItemMouse))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/wishlist"))
                .andExpect(jsonPath("$.totalItems").value(1));

        // ---------------------------------------------------------------------
        // 8. Requisito 4: el historico conserva TODO lo que paso por la lista,
        //    incluido el producto que ya no esta
        // ---------------------------------------------------------------------
        mockMvc.perform(get("/api/v1/wishlist/history"))
                .andExpect(status().isOk())
                // AGREGADO mouse, AGREGADO morral, ACTUALIZADO mouse, ELIMINADO mouse
                .andExpect(jsonPath("$.totalElementos").value(4));

        mockMvc.perform(get("/api/v1/wishlist/history").param("accion", "ELIMINADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(1)))
                .andExpect(jsonPath("$.contenido[0].productoNombre").value("Mouse inalambrico"))
                .andExpect(jsonPath("$.contenido[0].cantidadDeseada").value(99))
                // snapshot del precio en el momento del evento
                .andExpect(jsonPath("$.contenido[0].productoPrecio").value(54900.00));
    }

    @Test
    @DisplayName("rechaza una cantidad invalida con 400 y detalle del campo")
    void valida_la_cantidad() throws Exception {
        mockMvc.perform(post("/api/v1/wishlist/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productoId": %d, "cantidadDeseada": 0}
                                """.formatted(idMouse)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detalles", hasSize(1)))
                .andExpect(jsonPath("$.detalles[0]", containsString("cantidadDeseada")));
    }

    @Test
    @DisplayName("un producto inexistente responde 404")
    void producto_inexistente() throws Exception {
        mockMvc.perform(post("/api/v1/wishlist/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productoId": 987654, "cantidadDeseada": 1}
                                """))
                .andExpect(status().isNotFound());
    }
}
