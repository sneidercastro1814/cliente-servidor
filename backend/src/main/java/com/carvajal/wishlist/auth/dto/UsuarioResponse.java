package com.carvajal.wishlist.auth.dto;

import com.carvajal.wishlist.auth.domain.Role;
import com.carvajal.wishlist.auth.domain.User;

/** Usuario expuesto al cliente. Nunca incluye el hash de la contrasena. */
public record UsuarioResponse(Long id, String nombre, String email, Role rol) {

    public static UsuarioResponse de(User u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getEmail(), u.getRol());
    }
}
