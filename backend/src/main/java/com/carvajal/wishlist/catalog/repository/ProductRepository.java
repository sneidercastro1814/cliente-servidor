package com.carvajal.wishlist.catalog.repository;

import com.carvajal.wishlist.catalog.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Catalogo paginado con filtros opcionales.
     *
     * <p>El {@code @EntityGraph} trae la categoria en el mismo SELECT para
     * evitar el problema N+1 al armar la respuesta del catalogo.</p>
     */
    @EntityGraph(attributePaths = "category")
    @Query(value = """
            SELECT p FROM Product p
            WHERE p.activo = TRUE
              AND (:q IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:categoriaId IS NULL OR p.category.id = :categoriaId)
            """,
            countQuery = """
            SELECT COUNT(p) FROM Product p
            WHERE p.activo = TRUE
              AND (:q IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:categoriaId IS NULL OR p.category.id = :categoriaId)
            """)
    Page<Product> buscar(@Param("q") String q,
                         @Param("categoriaId") Long categoriaId,
                         Pageable pageable);

    /** Igual que {@link #buscar}, pero mostrando solo lo que tiene existencias. */
    @EntityGraph(attributePaths = "category")
    @Query(value = """
            SELECT p FROM Product p
            WHERE p.activo = TRUE
              AND p.stock > 0
              AND (:q IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:categoriaId IS NULL OR p.category.id = :categoriaId)
            """,
            countQuery = """
            SELECT COUNT(p) FROM Product p
            WHERE p.activo = TRUE
              AND p.stock > 0
              AND (:q IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:categoriaId IS NULL OR p.category.id = :categoriaId)
            """)
    Page<Product> buscarConStock(@Param("q") String q,
                                 @Param("categoriaId") Long categoriaId,
                                 Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findByIdAndActivoTrue(Long id);
}
