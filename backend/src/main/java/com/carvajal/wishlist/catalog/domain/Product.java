package com.carvajal.wishlist.catalog.domain;

import com.carvajal.wishlist.shared.domain.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Producto del catalogo.
 *
 * <p>La prueba exige como minimo ID, nombre, precio y cantidad en stock. Se
 * agregan sku, descripcion, marca, imagen y categoria para que el catalogo sea
 * realista y filtrable.</p>
 */
@Entity
@Table(name = "products")
public class Product extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String sku;

    @Column(nullable = false, length = 180)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    /** Cantidad existente. Es el dato que alimenta la notificacion de stock. */
    @Column(nullable = false)
    private Integer stock;

    @Column(length = 120)
    private String marca;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private boolean activo = true;

    protected Product() {
        // requerido por JPA
    }

    public Product(String sku, String nombre, BigDecimal precio, Integer stock, Category category) {
        this.sku = sku;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.category = category;
        this.activo = true;
    }

    // ---------------------------------------------------------------------
    // Reglas de negocio del producto
    // ---------------------------------------------------------------------

    /** @return true si queda al menos una unidad disponible. */
    public boolean tieneStock() {
        return stock != null && stock > 0;
    }

    /** @return true si el stock alcanza para la cantidad pedida. */
    public boolean cubreCantidad(int cantidadDeseada) {
        return stock != null && stock >= cantidadDeseada;
    }

    public int stockSeguro() {
        return stock == null ? 0 : stock;
    }

    // ---------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
