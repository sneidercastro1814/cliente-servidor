package com.carvajal.wishlist.auth.service;

import com.carvajal.wishlist.auth.domain.User;
import com.carvajal.wishlist.auth.repository.UserRepository;
import com.carvajal.wishlist.shared.exception.RecursoNoEncontradoException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resuelve quien es el cliente autenticado en la peticion actual.
 *
 * <p>Centralizarlo aqui tiene un efecto importante de seguridad: ningun
 * endpoint de la lista de deseos recibe el id del usuario por parametro, asi
 * que un cliente no puede pedir ni modificar la lista de otro cambiando la
 * URL. El dueno de la lista siempre sale del token.</p>
 */
@Service
@Transactional(readOnly = true)
public class UsuarioActualService {

    private final UserRepository userRepository;

    public UsuarioActualService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User obtener() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("No hay un usuario autenticado en la peticion");
        }
        String email = authentication.getName();
        return userRepository.findByEmailIgnoreCaseAndActivoTrue(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario del token ya no existe o esta inactivo: " + email));
    }
}
