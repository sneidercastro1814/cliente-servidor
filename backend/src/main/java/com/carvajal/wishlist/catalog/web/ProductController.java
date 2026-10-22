package com.carvajal.wishlist.catalog.web;

import com.carvajal.wishlist.catalog.dto.ProductoResponse;
import com.carvajal.wishlist.catalog.service.ProductService;
import com.carvajal.wishlist.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Catalogo de productos (lectura publica). */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Catalogo", description = "Productos que ofrece Carvajal, con sus cantidades existentes")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Lista el catalogo paginado indicando el stock de cada producto")
    public ResponseEntity<PageResponse<ProductoResponse>> catalogo(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "false") boolean soloConStock,
            @PageableDefault(size = 12, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(productService.catalogo(q, categoriaId, soloConStock, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un producto")
    public ResponseEntity<ProductoResponse> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(productService.detalle(id));
    }
}
