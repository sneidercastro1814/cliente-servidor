package com.carvajal.wishlist.wishlist.repository;

import com.carvajal.wishlist.wishlist.domain.AccionHistorial;
import com.carvajal.wishlist.wishlist.domain.WishlistItemHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistItemHistoryRepository extends JpaRepository<WishlistItemHistory, Long> {

    /** Historico completo de la lista, lo mas reciente primero. */
    Page<WishlistItemHistory> findByWishlistIdOrderByOccurredAtDesc(Long wishlistId, Pageable pageable);

    /** Historico filtrado por tipo de evento. */
    Page<WishlistItemHistory> findByWishlistIdAndAccionOrderByOccurredAtDesc(Long wishlistId,
                                                                            AccionHistorial accion,
                                                                            Pageable pageable);

    long countByWishlistId(Long wishlistId);
}
