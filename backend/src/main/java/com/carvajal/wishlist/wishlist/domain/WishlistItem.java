package com.carvajal.wishlist.wishlist.domain;

import com.carvajal.wishlist.catalog.domain.Product;
import com.carvajal.wishlist.shared.domain.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Un deseo concreto dentro de la lista: estado ACTUAL del producto deseado.
 *
 * <p>La restriccion unica (wishlist_id, product_id) impide que el mismo
 * producto quede duplicado en una lista. Gracias a eso, "agregar algo que ya
 * esta" se convierte en un error de negocio explicito y no en basura en la
 * base de datos.</p>
 */
@Entity
@Table(
        name = "wishlist_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_wishlist_items_producto",
                columnNames = {"wishlist_id", "product_id"}
        )
)
public class WishlistItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "cantidad_deseada", nullable = false)
    private Integer cantidadDeseada = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridad prioridad = Prioridad.MEDIA;

    @Column(length = 300)
    private String nota;

    protected WishlistItem() {
        // requerido por JPA
    }

    public WishlistItem(Wishlist wishlist, Product product, Integer cantidadDeseada,
                        Prioridad prioridad, String nota) {
        this.wishlist = wishlist;
        this.product = product;
        this.cantidadDeseada = cantidadDeseada;
        this.prioridad = prioridad;
        this.nota = nota;
    }

    /** Evalua este deseo contra el stock real del producto. */
    public EstadoStock estadoStock() {
        return EstadoStock.evaluar(product.stockSeguro(), cantidadDeseada);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Wishlist getWishlist() {
        return wishlist;
    }

    public void setWishlist(Wishlist wishlist) {
        this.wishlist = wishlist;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getCantidadDeseada() {
        return cantidadDeseada;
    }

    public void setCantidadDeseada(Integer cantidadDeseada) {
        this.cantidadDeseada = cantidadDeseada;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Prioridad prioridad) {
        this.prioridad = prioridad;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}
