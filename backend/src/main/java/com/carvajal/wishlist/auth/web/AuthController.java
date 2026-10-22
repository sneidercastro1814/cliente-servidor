package com.carvajal.wishlist.auth.web;

import com.carvajal.wishlist.auth.dto.LoginRequest;
import com.carvajal.wishlist.auth.dto.LoginResponse;
import com.carvajal.wishlist.auth.dto.UsuarioResponse;
import com.carvajal.wishlist.auth.service.AuthService;
import com.carvajal.wishlist.auth.service.UsuarioActualService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Login y emision de JWT")
public class AuthController {

    private final AuthService authService;
    private final UsuarioActualService usuarioActual;

    public AuthController(AuthService authService, UsuarioActualService usuarioActual) {
        this.authService = authService;
        this.usuarioActual = usuarioActual;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica al cliente y devuelve un JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Devuelve el cliente dueno del token enviado")
    public ResponseEntity<UsuarioResponse> yo() {
        return ResponseEntity.ok(UsuarioResponse.de(usuarioActual.obtener()));
    }
}
