package com.carvajal.wishlist.catalog.web;

import com.carvajal.wishlist.catalog.dto.CategoriaResponse;
import com.carvajal.wishlist.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Catalogo", description = "Productos que ofrece Carvajal, con sus cantidades existentes")
public class CategoryController {

    private final ProductService productService;

    public CategoryController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Categorias disponibles para filtrar el catalogo")
    public ResponseEntity<List<CategoriaResponse>> categorias() {
        return ResponseEntity.ok(productService.categorias());
    }
}
