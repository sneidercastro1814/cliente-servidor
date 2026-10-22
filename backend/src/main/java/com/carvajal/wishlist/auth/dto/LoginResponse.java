package com.carvajal.wishlist.auth.dto;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        UsuarioResponse usuario
) {
}
