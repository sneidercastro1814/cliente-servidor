package com.carvajal.wishlist.catalog;

import com.carvajal.wishlist.catalog.domain.Category;
import com.carvajal.wishlist.catalog.domain.Product;
import com.carvajal.wishlist.catalog.repository.CategoryRepository;
import com.carvajal.wishlist.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integracion del requisito funcional 1: el catalogo con las
 * cantidades existentes. Levanta el contexto completo de Spring y pasa por la
 * cadena de filtros de seguridad real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Catalogo de productos (integracion)")
class CatalogoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;

    @BeforeEach
    void prepararDatos() {
        Category tecnologia = categoryRepository.save(new Category("Tecnologia", "tecnologia", null));
        Category papeleria = categoryRepository.save(new Category("Papeleria", "papeleria", null));

        // El orden por nombre ascendente es el que usa el endpoint por defecto.
        productRepository.save(new Product("TEC-BAS-001", "Base refrigerante",
                new BigDecimal("79900.00"), 0, tecnologia));
        productRepository.save(new Product("PAP-CUA-001", "Cuaderno cosido",
                new BigDecimal("7900.00"), 240, papeleria));
        productRepository.save(new Product("TEC-MOU-001", "Mouse inalambrico",
                new BigDecimal("54900.00"), 41, tecnologia));
    }

    @Test
    @DisplayName("es publico y muestra las cantidades existentes de cada producto")
    void el_catalogo_es_publico_y_muestra_el_stock() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(3)))
                .andExpect(jsonPath("$.totalElementos").value(3))
                // ordenado por nombre: Base, Cuaderno, Mouse
                .andExpect(jsonPath("$.contenido[0].nombre").value("Base refrigerante"))
                .andExpect(jsonPath("$.contenido[0].stock").value(0))
                .andExpect(jsonPath("$.contenido[0].disponible").value(false))
                .andExpect(jsonPath("$.contenido[2].nombre").value("Mouse inalambrico"))
                .andExpect(jsonPath("$.contenido[2].stock").value(41))
                .andExpect(jsonPath("$.contenido[2].disponible").value(true))
                .andExpect(jsonPath("$.contenido[2].categoria.nombre").value("Tecnologia"));
    }

    @Test
    @DisplayName("filtra por texto en el nombre")
    void filtra_por_texto() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("q", "mouse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(1)))
                .andExpect(jsonPath("$.contenido[0].sku").value("TEC-MOU-001"));
    }

    @Test
    @DisplayName("puede ocultar los productos agotados")
    void puede_ocultar_los_agotados() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("soloConStock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(2)));
    }

    @Test
    @DisplayName("filtra por categoria")
    void filtra_por_categoria() throws Exception {
        Long idPapeleria = categoryRepository.findAllByOrderByNombreAsc().stream()
                .filter(c -> c.getSlug().equals("papeleria"))
                .findFirst()
                .orElseThrow()
                .getId();

        mockMvc.perform(get("/api/v1/products").param("categoriaId", idPapeleria.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(1)))
                .andExpect(jsonPath("$.contenido[0].nombre").value("Cuaderno cosido"));
    }

    @Test
    @DisplayName("un producto inexistente responde 404 con el formato de error unico")
    void producto_inexistente_responde_404() throws Exception {
        mockMvc.perform(get("/api/v1/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/products/999999"));
    }

    @Test
    @DisplayName("expone las categorias para filtrar")
    void expone_las_categorias() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("Papeleria"));
    }
}
