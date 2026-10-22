package com.carvajal.wishlist.wishlist.service;

import com.carvajal.wishlist.auth.domain.User;
import com.carvajal.wishlist.auth.service.UsuarioActualService;
import com.carvajal.wishlist.catalog.domain.Product;
import com.carvajal.wishlist.catalog.repository.ProductRepository;
import com.carvajal.wishlist.shared.exception.RecursoNoEncontradoException;
import com.carvajal.wishlist.shared.exception.ReglaDeNegocioException;
import com.carvajal.wishlist.shared.web.PageResponse;
import com.carvajal.wishlist.wishlist.domain.AccionHistorial;
import com.carvajal.wishlist.wishlist.domain.Wishlist;
import com.carvajal.wishlist.wishlist.domain.WishlistItem;
import com.carvajal.wishlist.wishlist.domain.WishlistItemHistory;
import com.carvajal.wishlist.wishlist.dto.ActualizarItemRequest;
import com.carvajal.wishlist.wishlist.dto.AgregarItemRequest;
import com.carvajal.wishlist.wishlist.dto.HistorialResponse;
import com.carvajal.wishlist.wishlist.dto.WishlistItemResponse;
import com.carvajal.wishlist.wishlist.dto.WishlistResponse;
import com.carvajal.wishlist.wishlist.repository.WishlistItemHistoryRepository;
import com.carvajal.wishlist.wishlist.repository.WishlistItemRepository;
import com.carvajal.wishlist.wishlist.repository.WishlistRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Logica de negocio de la lista de deseos. Es el corazon de la prueba: aqui
 * viven los requisitos funcionales 2, 3 y 4.
 *
 * <p>Toda operacion que cambia la lista deja su rastro en el historico dentro
 * de la MISMA transaccion. Si falla el registro del historico, se revierte el
 * cambio del item: nunca queda un movimiento sin huella.</p>
 */
@Service
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository itemRepository;
    private final WishlistItemHistoryRepository historyRepository;
    private final ProductRepository productRepository;
    private final UsuarioActualService usuarioActual;

    public WishlistService(WishlistRepository wishlistRepository,
                           WishlistItemRepository itemRepository,
                           WishlistItemHistoryRepository historyRepository,
                           ProductRepository productRepository,
                           UsuarioActualService usuarioActual) {
        this.wishlistRepository = wishlistRepository;
        this.itemRepository = itemRepository;
        this.historyRepository = historyRepository;
        this.productRepository = productRepository;
        this.usuarioActual = usuarioActual;
    }

    // =========================================================================
    // Requisito 2 (listar) + Requisito 3 (notificar stock)
    // =========================================================================

    /**
     * Devuelve la lista del cliente con la evaluacion de stock ya resuelta.
     *
     * <p>No es {@code readOnly} porque en el primer acceso puede tener que crear
     * la lista del cliente.</p>
     */
    public WishlistResponse consultar() {
        User user = usuarioActual.obtener();
        Wishlist lista = obtenerOCrear(user);

        Comparator<WishlistItem> orden = Comparator
                .comparing(WishlistItem::getPrioridad)
                .thenComparing((WishlistItem i) -> i.getProduct().getNombre());

        List<WishlistItemResponse> items = lista.getItems().stream()
                .sorted(orden)
                .map(WishlistItemResponse::de)
                .toList();

        // Requisito 3: el servidor arma los avisos, el cliente solo los pinta.
        List<String> notificaciones = items.stream()
                .filter(WishlistItemResponse::requiereNotificacion)
                .map(item -> "%s: %s".formatted(item.producto().nombre(), item.mensajeStock()))
                .toList();

        return new WishlistResponse(
                lista.getId(),
                lista.getNombre(),
                items.size(),
                notificaciones.size(),
                notificaciones,
                items
        );
    }

    // =========================================================================
    // Requisito 2 (agregar / actualizar / eliminar) + Requisito 4 (historico)
    // =========================================================================

    /**
     * Agrega un producto a la lista.
     *
     * <p>Se permite agregar productos agotados a proposito: es una lista de
     * DESEOS, el cliente puede querer algo que hoy no hay. Lo que la prueba
     * exige es avisarle, y de eso se encarga {@link #consultar()}.</p>
     */
    public WishlistItemResponse agregar(AgregarItemRequest request) {
        User user = usuarioActual.obtener();
        Wishlist lista = obtenerOCrear(user);

        Product producto = productRepository.findByIdAndActivoTrue(request.productoId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("el producto", request.productoId()));

        if (itemRepository.existsByWishlistIdAndProductId(lista.getId(), producto.getId())) {
            throw new ReglaDeNegocioException(
                    "El producto \"%s\" ya esta en tu lista de deseos".formatted(producto.getNombre()));
        }

        WishlistItem item = itemRepository.save(new WishlistItem(
                lista,
                producto,
                request.cantidadDeseada(),
                request.prioridadOrDefault(),
                request.nota()
        ));

        registrarHistorico(AccionHistorial.AGREGADO, lista, producto, user, item.getCantidadDeseada());

        return WishlistItemResponse.de(item);
    }

    /** Actualiza cantidad, prioridad o nota de un deseo. */
    public WishlistItemResponse actualizar(Long itemId, ActualizarItemRequest request) {
        User user = usuarioActual.obtener();
        Wishlist lista = obtenerOCrear(user);

        WishlistItem item = buscarItemPropio(itemId, lista);

        item.setCantidadDeseada(request.cantidadDeseada());
        if (request.prioridad() != null) {
            item.setPrioridad(request.prioridad());
        }
        item.setNota(request.nota());

        WishlistItem actualizado = itemRepository.save(item);

        registrarHistorico(AccionHistorial.ACTUALIZADO, lista, actualizado.getProduct(),
                user, actualizado.getCantidadDeseada());

        return WishlistItemResponse.de(actualizado);
    }

    /**
     * Elimina un deseo de la lista.
     *
     * <p>El historico se registra ANTES del borrado: es exactamente lo que
     * conserva el rastro del producto que "llego a la lista" aunque hoy ya no
     * este en ella.</p>
     */
    public void eliminar(Long itemId) {
        User user = usuarioActual.obtener();
        Wishlist lista = obtenerOCrear(user);

        WishlistItem item = buscarItemPropio(itemId, lista);

        registrarHistorico(AccionHistorial.ELIMINADO, lista, item.getProduct(),
                user, item.getCantidadDeseada());

        itemRepository.delete(item);
    }

    /** Historico completo de la lista, lo mas reciente primero. */
    @Transactional(readOnly = true)
    public PageResponse<HistorialResponse> historial(AccionHistorial accion, Pageable pageable) {
        User user = usuarioActual.obtener();
        Wishlist lista = wishlistRepository.findFirstByUserId(user.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El cliente aun no tiene lista de deseos"));

        Page<WishlistItemHistory> pagina = (accion == null)
                ? historyRepository.findByWishlistIdOrderByOccurredAtDesc(lista.getId(), pageable)
                : historyRepository.findByWishlistIdAndAccionOrderByOccurredAtDesc(lista.getId(), accion, pageable);

        return PageResponse.de(pagina, HistorialResponse::de);
    }

    // =========================================================================
    // Apoyo
    // =========================================================================

    private Wishlist obtenerOCrear(User user) {
        return wishlistRepository.findByUserId(user.getId())
                .orElseGet(() -> wishlistRepository.save(new Wishlist(user)));
    }

    /**
     * Busca el item exigiendo que pertenezca a la lista del cliente autenticado.
     * Si el id es de otro cliente, responde 404 y no 403: no confirmamos la
     * existencia de recursos ajenos.
     */
    private WishlistItem buscarItemPropio(Long itemId, Wishlist lista) {
        return itemRepository.findByIdAndWishlistId(itemId, lista.getId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("el deseo", itemId));
    }

    private void registrarHistorico(AccionHistorial accion, Wishlist lista, Product producto,
                                    User user, int cantidadDeseada) {
        historyRepository.save(
                WishlistItemHistory.registrar(accion, lista, producto, user, cantidadDeseada));
    }
}
