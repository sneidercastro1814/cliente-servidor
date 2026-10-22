package com.carvajal.wishlist.wishlist.web;

import com.carvajal.wishlist.shared.web.PageResponse;
import com.carvajal.wishlist.wishlist.domain.AccionHistorial;
import com.carvajal.wishlist.wishlist.dto.ActualizarItemRequest;
import com.carvajal.wishlist.wishlist.dto.AgregarItemRequest;
import com.carvajal.wishlist.wishlist.dto.HistorialResponse;
import com.carvajal.wishlist.wishlist.dto.WishlistItemResponse;
import com.carvajal.wishlist.wishlist.dto.WishlistResponse;
import com.carvajal.wishlist.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lista de deseos del cliente autenticado.
 *
 * <p>Ningun endpoint recibe el id del usuario: siempre sale del JWT. Asi un
 * cliente no puede ver ni tocar la lista de otro.</p>
 */
@RestController
@RequestMapping("/api/v1/wishlist")
@Tag(name = "Lista de deseos", description = "Listar, agregar, actualizar y eliminar deseos, con historico")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    @Operation(summary = "Consulta la lista e informa si algun deseo quedo sin stock")
    public ResponseEntity<WishlistResponse> consultar() {
        return ResponseEntity.ok(wishlistService.consultar());
    }

    @PostMapping("/items")
    @Operation(summary = "Agrega un producto a la lista de deseos")
    public ResponseEntity<WishlistItemResponse> agregar(@Valid @RequestBody AgregarItemRequest request) {
        WishlistItemResponse creado = wishlistService.agregar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Actualiza la cantidad, prioridad o nota de un deseo")
    public ResponseEntity<WishlistItemResponse> actualizar(@PathVariable Long itemId,
                                                           @Valid @RequestBody ActualizarItemRequest request) {
        return ResponseEntity.ok(wishlistService.actualizar(itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Elimina un deseo (el rastro queda en el historico)")
    public ResponseEntity<Void> eliminar(@PathVariable Long itemId) {
        wishlistService.eliminar(itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    @Operation(summary = "Historico de todo lo que ha pasado por la lista de deseos")
    public ResponseEntity<PageResponse<HistorialResponse>> historial(
            @RequestParam(required = false) AccionHistorial accion,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(wishlistService.historial(accion, pageable));
    }
}
