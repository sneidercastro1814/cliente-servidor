package com.carvajal.wishlist.wishlist.repository;

import com.carvajal.wishlist.wishlist.domain.WishlistItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    /**
     * Busca un item exigiendo tambien la lista a la que pertenece.
     *
     * <p>Esto no es un detalle cosmetico: es la defensa contra que un cliente
     * modifique o borre un deseo de la lista de otro cliente mandando un id
     * ajeno en la URL.</p>
     */
    @EntityGraph(attributePaths = {"product", "product.category"})
    Optional<WishlistItem> findByIdAndWishlistId(Long id, Long wishlistId);

    boolean existsByWishlistIdAndProductId(Long wishlistId, Long productId);

    @EntityGraph(attributePaths = {"product", "product.category"})
    Optional<WishlistItem> findByWishlistIdAndProductId(Long wishlistId, Long productId);
}
