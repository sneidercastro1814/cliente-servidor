package com.carvajal.wishlist.shared.web;

import com.carvajal.wishlist.shared.exception.RecursoNoEncontradoException;
import com.carvajal.wishlist.shared.exception.ReglaDeNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Traduce las excepciones del dominio a respuestas HTTP con un formato unico.
 * Centralizarlo aqui evita bloques try/catch repartidos por los controladores.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiError.de(404, "No encontrado", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ResponseEntity<ApiError> reglaDeNegocio(ReglaDeNegocioException ex, HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiError.de(409, "Conflicto", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> credencialesInvalidas(BadCredentialsException ex, HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.de(401, "No autorizado", "Correo o contrasena incorrectos", req.getRequestURI()));
    }

    /** Errores de validacion de los DTO de entrada ({@code @Valid}). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(this::describir)
                .toList();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.conDetalles(400, "Solicitud invalida",
                        "Revisa los datos enviados", req.getRequestURI(), detalles));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.de(500, "Error interno",
                        "Ocurrio un error procesando la solicitud", req.getRequestURI()));
    }

    private String describir(FieldError error) {
        return "%s: %s".formatted(error.getField(), error.getDefaultMessage());
    }
}
