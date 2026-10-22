package com.carvajal.wishlist.wishlist;

import com.carvajal.wishlist.auth.domain.Role;
import com.carvajal.wishlist.auth.domain.User;
import com.carvajal.wishlist.auth.service.UsuarioActualService;
import com.carvajal.wishlist.catalog.domain.Category;
import com.carvajal.wishlist.catalog.domain.Product;
import com.carvajal.wishlist.catalog.repository.ProductRepository;
import com.carvajal.wishlist.shared.exception.RecursoNoEncontradoException;
import com.carvajal.wishlist.shared.exception.ReglaDeNegocioException;
import com.carvajal.wishlist.wishlist.domain.AccionHistorial;
import com.carvajal.wishlist.wishlist.domain.EstadoStock;
import com.carvajal.wishlist.wishlist.domain.Prioridad;
import com.carvajal.wishlist.wishlist.domain.Wishlist;
import com.carvajal.wishlist.wishlist.domain.WishlistItem;
import com.carvajal.wishlist.wishlist.domain.WishlistItemHistory;
import com.carvajal.wishlist.wishlist.dto.ActualizarItemRequest;
import com.carvajal.wishlist.wishlist.dto.AgregarItemRequest;
import com.carvajal.wishlist.wishlist.dto.WishlistItemResponse;
import com.carvajal.wishlist.wishlist.dto.WishlistResponse;
import com.carvajal.wishlist.wishlist.repository.WishlistItemHistoryRepository;
import com.carvajal.wishlist.wishlist.repository.WishlistItemRepository;
import com.carvajal.wishlist.wishlist.repository.WishlistRepository;
import com.carvajal.wishlist.wishlist.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de la logica de la lista de deseos.
 *
 * <p>Los repositorios van simulados con Mockito: lo que se prueba aqui son las
 * reglas de negocio de los requisitos 2, 3 y 4, sin base de datos de por medio.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Servicio de lista de deseos")
class WishlistServiceTest {

    @Mock private WishlistRepository wishlistRepository;
    @Mock private WishlistItemRepository itemRepository;
    @Mock private WishlistItemHistoryRepository historyRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UsuarioActualService usuarioActual;

    @InjectMocks private WishlistService service;

    private User cliente;
    private Product mouse;
    private Product agotado;
    private Wishlist lista;

    @BeforeEach
    void setUp() {
        cliente = new User("Laura Gomez", "cliente@carvajal.com", "$2b$10$hash", Role.CLIENTE);
        cliente.setId(1L);

        Category categoria = new Category("Tecnologia", "tecnologia", null);
        categoria.setId(4L);

        mouse = new Product("TEC-MOU-001", "Mouse inalambrico", new BigDecimal("54900.00"), 10, categoria);
        mouse.setId(15L);

        agotado = new Product("ESC-MOC-001", "Morral escolar", new BigDecimal("89900.00"), 0, categoria);
        agotado.setId(9L);

        lista = new Wishlist(cliente);
        lista.setId(1L);
    }

    // =========================================================================
    // Requisito 2: agregar
    // =========================================================================

    @Test
    @DisplayName("agregar un producto guarda el deseo y deja el evento en el historico")
    void agregar_guarda_el_item_y_registra_el_historico() {
        when(usuarioActual.obtener()).thenReturn(cliente);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(lista));
        when(productRepository.findByIdAndActivoTrue(15L)).thenReturn(Optional.of(mouse));
        when(itemRepository.existsByWishlistIdAndProductId(1L, 15L)).thenReturn(false);
        when(itemRepository.save(any(WishlistItem.class))).thenAnswer(invocacion -> {
            WishlistItem guardado = invocacion.getArgument(0);
            guardado.setId(100L);
            return guardado;
        });

        WishlistItemResponse respuesta =
                service.agregar(new AgregarItemRequest(15L, 2, Prioridad.ALTA, "para la oficina"));

        assertThat(respuesta.id()).isEqualTo(100L);
        assertThat(respuesta.cantidadDeseada()).isEqualTo(2);
        assertThat(respuesta.estadoStock()).isEqualTo(EstadoStock.DISPONIBLE);

        ArgumentCaptor<WishlistItemHistory> captor = ArgumentCaptor.forClass(WishlistItemHistory.class);
        verify(historyRepository).save(captor.capture());

        WishlistItemHistory evento = captor.getValue();
        assertThat(evento.getAccion()).isEqualTo(AccionHistorial.AGREGADO);
        assertThat(evento.getCantidadDeseada()).isEqualTo(2);
        // el snapshot del producto queda congelado en el evento
        assertThat(evento.getProductoNombre()).isEqualTo("Mouse inalambrico");
        assertThat(evento.getProductoPrecio()).isEqualByComparingTo("54900.00");
        assertThat(evento.getStockAlMomento()).isEqualTo(10);
    }

    @Test
    @DisplayName("se puede desear un producto agotado: lo que importa es avisar, no bloquear")
    void permite_agregar_un_producto_agotado() {
        when(usuarioActual.obtener()).thenReturn(cliente);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(lista));
        when(productRepository.findByIdAndActivoTrue(9L)).thenReturn(Optional.of(agotado));
        when(itemRepository.existsByWishlistIdAndProductId(1L, 9L)).thenReturn(false);
        when(itemRepository.save(any(WishlistItem.class))).thenAnswer(i -> i.getArgument(0));

        WishlistItemResponse respuesta =
                service.agregar(new AgregarItemRequest(9L, 1, null, null));

        assertThat(respuesta.estadoStock()).isEqualTo(EstadoStock.SIN_STOCK);
        assertThat(respuesta.requiereNotificacion()).isTrue();
        assertThat(respuesta.prioridad()).isEqualTo(Prioridad.MEDIA); // valor por defecto
    }

    @Test
    @DisplayName("no deja duplicar el mismo producto en la lista")
    void rechaza_producto_duplicado() {
        when(usuarioActual.obtener()).thenReturn(cliente);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(lista));
        when(productRepository.findByIdAndActivoTrue(15L)).thenReturn(Optional.of(mouse));
        when(itemRepository.existsByWishlistIdAndProductId(1L, 15L)).thenReturn(true);

        assertThatThrownBy(() -> service.agregar(new AgregarItemRequest(15L, 1, null, null)))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("ya esta en tu lista");

        verify(itemRepository, never()).save(any(WishlistItem.class));
        verify(historyRepository, never()).save(any(WishlistItemHistory.class));
    }

    @Test
    @DisplayName("falla con 404 si el producto no existe")
    void falla_si_el_producto_no_existe() {
        when(usuarioActual.obtener()).thenReturn(cliente);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(lista));
        when(productRepository.findByIdAndActivoTrue(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.agregar(new AgregarItemRequest(999L, 1, null, null)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // =========================================================================
    // Requisito 4: historico
    // =========================================================================

    @Test
    @DisplayName("al eliminar, el historico se escribe ANTES del borrado")
    void eliminar_registra_el_historico_antes_de_borrar() {
        WishlistItem item = new WishlistItem(lista, mouse, 3, Prioridad.MEDIA, null);
        item.setId(50L);

        when(usuarioActual.obtener()).thenReturn(cliente);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(lista));
        when(itemRepository.findByIdAndWishlistId(50L, 1L)).thenReturn(Optional.of(item));

        service.eliminar(50L);

        // El orden es la garantia de que el rastro no se pierde.
        InOrder orden = inOrder(historyRepository, itemRepository);
        ArgumentCaptor<WishlistItemHistory> captor = ArgumentCaptor.forClass(WishlistItemHistory.class);
        orden.verify(historyRepository).save(captor.capture());
        orden.verify(itemRepository).delete(item);

        assertThat(captor.getValue().getAccion()).isEqualTo(AccionHistorial.ELIMINADO);
        assertThat(captor.getValue().getCantidadDeseada()).isEqualTo(3);
    }

    @Test
    @DisplayName("actualizar cambia el deseo y registra el evento ACTUALIZADO")
    void actualizar_registra_el_evento() {
        WishlistItem item = new WishlistItem(lista, mouse, 1, Prioridad.BAJA, null);
        item.setId(50L);

        when(usuarioActual.obtener()).thenReturn(cliente);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(lista));
        when(itemRepository.findByIdAndWishlistId(50L, 1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(WishlistItem.class))).thenAnswer(i -> i.getArgument(0));

        WishlistItemResponse respuesta =
                service.actualizar(50L, new ActualizarItemRequest(7, Prioridad.ALTA, "subo la cantidad"));

        assertThat(respuesta.cantidadDeseada()).isEqualTo(7);
        assertThat(respuesta.prioridad()).isEqualTo(Prioridad.ALTA);
        // 7 deseados contra 10 en stock sigue siendo suficiente
        assertThat(respuesta.estadoStock()).isEqualTo(EstadoStock.DISPONIBLE);

        ArgumentCaptor<WishlistItemHistory> captor = ArgumentCaptor.forClass(WishlistItemHistory.class);
        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getAccion()).isEqualTo(AccionHistorial.ACTUALIZADO);
    }

    @Test
    @DisplayName("no permite tocar un deseo que no es de la lista del cliente")
    void no_permite_tocar_un_deseo_ajeno() {
        when(usuarioActual.obtener()).thenReturn(cliente);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(lista));
        when(itemRepository.findByIdAndWishlistId(777L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(777L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(itemRepository, never()).delete(any(WishlistItem.class));
        verify(historyRepository, never()).save(any(WishlistItemHistory.class));
    }

    // =========================================================================
    // Requisito 3: notificacion de stock
    // =========================================================================

    @Test
    @DisplayName("consultar la lista avisa de los deseos que se quedaron sin stock")
    void consultar_notifica_los_deseos_sin_stock() {
        WishlistItem disponible = new WishlistItem(lista, mouse, 1, Prioridad.MEDIA, null);
        disponible.setId(1L);
        WishlistItem sinStock = new WishlistItem(lista, agotado, 2, Prioridad.ALTA, null);
        sinStock.setId(2L);
        lista.getItems().addAll(List.of(disponible, sinStock));

        when(usuarioActual.obtener()).thenReturn(cliente);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(lista));

        WishlistResponse respuesta = service.consultar();

        assertThat(respuesta.totalItems()).isEqualTo(2);
        assertThat(respuesta.itemsConNotificacion()).isEqualTo(1);
        assertThat(respuesta.notificaciones())
                .hasSize(1)
                .first().asString().contains("Morral escolar");

        assertThat(respuesta.items()).anySatisfy(item -> {
            assertThat(item.producto().nombre()).isEqualTo("Morral escolar");
            assertThat(item.estadoStock()).isEqualTo(EstadoStock.SIN_STOCK);
            assertThat(item.requiereNotificacion()).isTrue();
        });

        // el deseo con prioridad ALTA se muestra primero
        assertThat(respuesta.items().get(0).prioridad()).isEqualTo(Prioridad.ALTA);
    }

    @Test
    @DisplayName("una lista sin deseos no genera notificaciones")
    void lista_vacia_no_notifica() {
        when(usuarioActual.obtener()).thenReturn(cliente);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(lista));

        WishlistResponse respuesta = service.consultar();

        assertThat(respuesta.totalItems()).isZero();
        assertThat(respuesta.itemsConNotificacion()).isZero();
        assertThat(respuesta.notificaciones()).isEmpty();
    }
}
