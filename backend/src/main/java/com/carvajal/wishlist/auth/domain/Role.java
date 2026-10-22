package com.carvajal.wishlist.auth.domain;

/**
 * Roles del sistema. Se persisten como texto ({@code EnumType.STRING}) para que
 * la base de datos sea legible y para no depender del orden del enum.
 */
public enum Role {
    CLIENTE,
    ADMIN
}
