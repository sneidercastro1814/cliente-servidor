package com.carvajal.wishlist.catalog.service;

import com.carvajal.wishlist.catalog.domain.Product;
import com.carvajal.wishlist.catalog.dto.CategoriaResponse;
import com.carvajal.wishlist.catalog.dto.ProductoResponse;
import com.carvajal.wishlist.catalog.repository.CategoryRepository;
import com.carvajal.wishlist.catalog.repository.ProductRepository;
import com.carvajal.wishlist.shared.exception.RecursoNoEncontradoException;
import com.carvajal.wishlist.shared.web.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Catalogo de productos.
 *
 * <p>Resuelve el requisito funcional 1: "cuando el cliente ingrese, debe ver el
 * catalogo de productos que ofrece Carvajal, indicando las cantidades
 * existentes del mismo".</p>
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Catalogo paginado.
     *
     * @param q             texto a buscar en el nombre (opcional)
     * @param categoriaId   filtro por categoria (opcional)
     * @param soloConStock  si es true oculta los agotados; por defecto se
     *                      muestran todos, con su cantidad real, porque el
     *                      cliente puede desear algo que esta agotado
     */
    public PageResponse<ProductoResponse> catalogo(String q,
                                                   Long categoriaId,
                                                   boolean soloConStock,
                                                   Pageable pageable) {
        String texto = normalizar(q);
        Page<Product> pagina = soloConStock
                ? productRepository.buscarConStock(texto, categoriaId, pageable)
                : productRepository.buscar(texto, categoriaId, pageable);
        return PageResponse.de(pagina, ProductoResponse::de);
    }

    public ProductoResponse detalle(Long id) {
        return productRepository.findByIdAndActivoTrue(id)
                .map(ProductoResponse::de)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el producto", id));
    }

    public List<CategoriaResponse> categorias() {
        return categoryRepository.findAllByOrderByNombreAsc().stream()
                .map(CategoriaResponse::de)
                .toList();
    }

    /** Convierte cadenas vacias en null para que el filtro no aplique. */
    private String normalizar(String q) {
        return (q == null || q.isBlank()) ? null : q.trim();
    }
}
