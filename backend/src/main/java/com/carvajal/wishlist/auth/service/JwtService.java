package com.carvajal.wishlist.auth.service;

import com.carvajal.wishlist.auth.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Emision de tokens JWT. */
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationMinutes;
    private final String issuer;

    public JwtService(JwtEncoder jwtEncoder,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes,
                      @Value("${app.jwt.issuer}") String issuer) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
        this.issuer = issuer;
    }

    /**
     * Genera el token del cliente.
     *
     * <p>El {@code subject} es el correo: es lo que lee
     * {@code UsuarioActualService} para saber de quien es la lista de deseos en
     * cada peticion.</p>
     */
    public String generarToken(User user) {
        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .claim("nombre", user.getNombre())
                .claim("roles", List.of(user.getRol().name()))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long expiracionSegundos() {
        return expirationMinutes * 60;
    }
}
