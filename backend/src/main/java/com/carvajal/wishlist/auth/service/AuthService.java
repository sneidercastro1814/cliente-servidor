package com.carvajal.wishlist.auth.service;

import com.carvajal.wishlist.auth.domain.User;
import com.carvajal.wishlist.auth.dto.LoginRequest;
import com.carvajal.wishlist.auth.dto.LoginResponse;
import com.carvajal.wishlist.auth.dto.UsuarioResponse;
import com.carvajal.wishlist.auth.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Autenticacion por correo y contrasena, con emision de JWT. */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCaseAndActivoTrue(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        // Comparacion contra el hash BCrypt almacenado; la contrasena en texto
        // claro nunca se guarda ni se registra en los logs.
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        return new LoginResponse(
                jwtService.generarToken(user),
                "Bearer",
                jwtService.expiracionSegundos(),
                UsuarioResponse.de(user)
        );
    }
}
