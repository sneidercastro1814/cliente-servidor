package com.carvajal.wishlist.auth;

import com.carvajal.wishlist.auth.domain.Role;
import com.carvajal.wishlist.auth.domain.User;
import com.carvajal.wishlist.auth.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integracion del plus de JWT: login real, token real y acceso a un
 * recurso protegido con ese token.
 *
 * <p>Esta clase no usa {@code @WithMockUser} a proposito: aqui lo que se valida
 * es la cadena de seguridad completa, no la logica de negocio.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Autenticacion con JWT (integracion)")
class AuthIntegrationTest {

    private static final String EMAIL = "cliente@carvajal.com";
    private static final String CLAVE = "Carvajal2026*";

    /** El mismo hash que trae el script V2__seed_data.sql. */
    private static final String HASH_DEL_SEED =
            "$2b$10$7MMkcP7b1LEOqDKqF.8ddOWXGIJy0DXi7QJwbwf..8rRprEW0BcTy";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararDatos() {
        userRepository.save(new User("Laura Gomez", EMAIL, passwordEncoder.encode(CLAVE), Role.CLIENTE));
    }

    @Test
    @DisplayName("el login devuelve un token que abre la lista de deseos")
    void login_devuelve_token_que_abre_la_lista() throws Exception {
        String respuesta = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(EMAIL, CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEnSegundos").value(1800))
                .andExpect(jsonPath("$.usuario.email").value(EMAIL))
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"))
                // la respuesta jamas debe exponer el hash
                .andExpect(jsonPath("$.usuario.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(respuesta, "$.token");

        mockMvc.perform(get("/api/v1/wishlist")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    @DisplayName("sin token, la lista de deseos responde 401")
    void sin_token_responde_401() throws Exception {
        mockMvc.perform(get("/api/v1/wishlist"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("un token falso responde 401")
    void token_invalido_responde_401() throws Exception {
        mockMvc.perform(get("/api/v1/wishlist")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer esto.no.es-un-token-valido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("con la clave equivocada responde 401 y no filtra si el correo existe")
    void clave_incorrecta_responde_401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "claveQueNoEs"}
                                """.formatted(EMAIL)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "noexiste@carvajal.com", "password": "%s"}
                                """.formatted(CLAVE)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("un correo mal formado responde 400")
    void correo_invalido_responde_400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "esto-no-es-un-correo", "password": "algo"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("el hash que trae el seed corresponde a la clave documentada en el README")
    void el_hash_del_seed_corresponde_a_la_clave_del_readme() {
        // Si alguien cambia el seed o el README y se desalinean, esta prueba falla.
        assertThat(passwordEncoder.matches(CLAVE, HASH_DEL_SEED)).isTrue();
        assertThat(passwordEncoder.matches("otraClave", HASH_DEL_SEED)).isFalse();
    }
}
