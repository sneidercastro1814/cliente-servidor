package com.carvajal.wishlist.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "el correo es obligatorio")
        @Email(message = "el correo no tiene un formato valido")
        String email,

        @NotBlank(message = "la contrasena es obligatoria")
        String password
) {
}
