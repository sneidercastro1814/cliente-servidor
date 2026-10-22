package com.carvajal.wishlist.auth.domain;

import com.carvajal.wishlist.shared.domain.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Cliente de la tienda. La prueba permite asumir que el usuario ya existe y
 * esta autenticado; la entidad se modela de todas formas porque la lista de
 * deseos le pertenece a un usuario y porque el plus de JWT necesita validar
 * credenciales contra la base de datos.
 */
@Entity
@Table(name = "users")
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    /** Hash BCrypt. Nunca se expone en los DTO de respuesta. */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Role rol = Role.CLIENTE;

    @Column(nullable = false)
    private boolean activo = true;

    protected User() {
        // requerido por JPA
    }

    public User(String nombre, String email, String passwordHash, Role rol) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRol() {
        return rol;
    }

    public void setRol(Role rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
