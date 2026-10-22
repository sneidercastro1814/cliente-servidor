package com.carvajal.wishlist.wishlist.domain;

import com.carvajal.wishlist.auth.domain.User;
import com.carvajal.wishlist.catalog.domain.Product;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro historico e inmutable de todo lo que ha pasado por la lista de deseos.
 *
 * <p>Esta entidad resuelve el requisito funcional 4. Dos decisiones de diseno
 * que vale la pena sustentar:</p>
 *
 * <ol>
 *   <li><b>Tabla de eventos, no borrado logico.</b> Si marcaramos los items como
 *       "eliminados" en {@code wishlist_items}, perderiamos los cambios
 *       intermedios de cantidad y chocariamos con la restriccion de unicidad
 *       cuando el cliente vuelva a agregar el mismo producto.</li>
 *   <li><b>Snapshot del producto.</b> Guardamos nombre, precio y stock tal como
 *       estaban en el momento del evento. Asi el historico queda fiel a los
 *       hechos y no se altera si el producto cambia de precio despues.</li>
 * </ol>
 */
@Entity
@Table(name = "wishlist_item_history")
public class WishlistItemHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wishlist_id", nullable = false, updatable = false)
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private AccionHistorial accion;

    @Column(name = "cantidad_deseada", nullable = false, updatable = false)
    private Integer cantidadDeseada;

    @Column(name = "producto_nombre", nullable = false, length = 180, updatable = false)
    private String productoNombre;

    @Column(name = "producto_precio", nullable = false, precision = 12, scale = 2, updatable = false)
    private BigDecimal productoPrecio;

    @Column(name = "stock_al_momento", nullable = false, updatable = false)
    private Integer stockAlMomento;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    protected WishlistItemHistory() {
        // requerido por JPA
    }

    /**
     * Construye un evento tomando el snapshot del producto en este instante.
     * Es el unico camino para crear historico, para que nunca quede un evento
     * sin snapshot.
     */
    public static WishlistItemHistory registrar(AccionHistorial accion,
                                                Wishlist wishlist,
                                                Product product,
                                                User user,
                                                int cantidadDeseada) {
        WishlistItemHistory h = new WishlistItemHistory();
        h.accion = accion;
        h.wishlist = wishlist;
        h.product = product;
        h.user = user;
        h.cantidadDeseada = cantidadDeseada;
        h.productoNombre = product.getNombre();
        h.productoPrecio = product.getPrecio();
        h.stockAlMomento = product.stockSeguro();
        h.occurredAt = LocalDateTime.now();
        return h;
    }

    public Long getId() {
        return id;
    }

    public Wishlist getWishlist() {
        return wishlist;
    }

    public Product getProduct() {
        return product;
    }

    public User getUser() {
        return user;
    }

    public AccionHistorial getAccion() {
        return accion;
    }

    public Integer getCantidadDeseada() {
        return cantidadDeseada;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public BigDecimal getProductoPrecio() {
        return productoPrecio;
    }

    public Integer getStockAlMomento() {
        return stockAlMomento;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
