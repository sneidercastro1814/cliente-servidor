package com.carvajal.wishlist.wishlist.repository;

import com.carvajal.wishlist.wishlist.domain.Wishlist;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /**
     * Trae la lista con sus items, los productos y las categorias en una sola
     * consulta. Es la clave para que {@code GET /api/v1/wishlist} no dispare
     * una consulta por cada deseo al evaluar el stock.
     */
    @EntityGraph(attributePaths = {"items", "items.product", "items.product.category"})
    Optional<Wishlist> findByUserId(Long userId);

    Optional<Wishlist> findFirstByUserId(Long userId);
}
