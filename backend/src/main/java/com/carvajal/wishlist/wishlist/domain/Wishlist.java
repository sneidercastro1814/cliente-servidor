package com.carvajal.wishlist.wishlist.domain;

import com.carvajal.wishlist.auth.domain.User;
import com.carvajal.wishlist.shared.domain.Auditable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista de deseos de un cliente. Una por usuario (restriccion unica en BD).
 */
@Entity
@Table(name = "wishlists")
public class Wishlist extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 120)
    private String nombre = "Mi lista de deseos";

    /**
     * Items actuales. {@code orphanRemoval} permite que al quitar un item de
     * esta coleccion el ORM lo borre de la tabla; el rastro no se pierde porque
     * queda registrado aparte en {@link WishlistItemHistory}.
     */
    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WishlistItem> items = new ArrayList<>();

    protected Wishlist() {
        // requerido por JPA
    }

    public Wishlist(User user) {
        this.user = user;
    }

    public void agregarItem(WishlistItem item) {
        item.setWishlist(this);
        this.items.add(item);
    }

    public void quitarItem(WishlistItem item) {
        this.items.remove(item);
        item.setWishlist(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<WishlistItem> getItems() {
        return items;
    }

    public void setItems(List<WishlistItem> items) {
        this.items = items;
    }
}
