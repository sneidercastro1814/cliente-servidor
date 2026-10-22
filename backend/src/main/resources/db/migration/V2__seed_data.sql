-- =============================================================================
-- Prueba Tecnica Carvajal - Lista de Deseos
-- V2: Datos simulados
--
-- La prueba indica: "Usted debe simular la informacion de los productos" y
-- "Asuma que el usuario ya existe y se encuentra autenticado en el sistema".
--
-- El set de datos esta armado a proposito para que los 4 requisitos funcionales
-- se puedan demostrar sin tocar nada:
--   * Hay productos con stock amplio, con stock bajo y con stock en CERO.
--   * La lista del cliente ya trae un producto agotado y uno con stock
--     insuficiente, para que la notificacion del requisito 3 se vea de una.
--   * El historico ya trae un evento ELIMINADO, para demostrar el requisito 4:
--     el producto ya no esta en la lista, pero el rastro sigue ahi.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Usuarios
-- Contrasenas cifradas con BCrypt (cost 10). En texto claro:
--   cliente@carvajal.com -> Carvajal2026*
--   admin@carvajal.com   -> Admin2026*
-- -----------------------------------------------------------------------------
INSERT INTO users (id, nombre, email, password_hash, rol, activo, created_at) VALUES
  (1, 'Laura Gomez',      'cliente@carvajal.com', '$2b$10$7MMkcP7b1LEOqDKqF.8ddOWXGIJy0DXi7QJwbwf..8rRprEW0BcTy', 'CLIENTE', 1, NOW(6)),
  (2, 'Admin Carvajal',   'admin@carvajal.com',   '$2b$10$G5XjhioapEDSoAOC5wiHoOAylkKh90QZZ4EaYnH17Mv1kTTYettuS', 'ADMIN',   1, NOW(6));

-- -----------------------------------------------------------------------------
-- Categorias
-- -----------------------------------------------------------------------------
INSERT INTO categories (id, nombre, slug, descripcion) VALUES
  (1, 'Papeleria',  'papeleria',  'Cuadernos, agendas, blocks y papel'),
  (2, 'Escolar',    'escolar',    'Utiles y material escolar'),
  (3, 'Oficina',    'oficina',    'Articulos y organizacion de oficina'),
  (4, 'Tecnologia', 'tecnologia', 'Accesorios tecnologicos de escritorio'),
  (5, 'Libros',     'libros',     'Libros y material de lectura');

-- -----------------------------------------------------------------------------
-- Productos (precios en COP)
-- -----------------------------------------------------------------------------
INSERT INTO products (id, sku, nombre, descripcion, precio, stock, marca, imagen_url, category_id, activo, created_at) VALUES
  (1,  'PAP-CUA-001', 'Cuaderno cosido 100 hojas cuadriculado', 'Cuaderno cosido tamano carta, 100 hojas, pasta dura.',            7900.00,  240, 'Norma',     NULL, 1, 1, NOW(6)),
  (2,  'PAP-CUA-002', 'Cuaderno argollado 5 materias',          'Cuaderno argollado 180 hojas con separadores de colores.',       23500.00,   85, 'Norma',     NULL, 1, 1, NOW(6)),
  (3,  'PAP-AGE-001', 'Agenda ejecutiva 2027 cuero sintetico',  'Agenda diaria con marcador de pagina y porta lapiz.',            48900.00,   12, 'Carvajal',  NULL, 1, 1, NOW(6)),
  (4,  'PAP-RES-001', 'Resma papel carta 75g (500 hojas)',      'Papel bond blanco carta, 75 gramos, caja por 500 hojas.',        18200.00,  500, 'Reprograf', NULL, 1, 1, NOW(6)),
  (5,  'PAP-BLO-001', 'Block iris carta x 40 hojas',            'Block de papel iris en colores surtidos.',                        9400.00,    0, 'Norma',     NULL, 1, 1, NOW(6)),

  (6,  'ESC-COL-001', 'Colores x 36 unidades largos',           'Caja de 36 colores de madera, mina resistente.',                 32900.00,   64, 'Norma',     NULL, 2, 1, NOW(6)),
  (7,  'ESC-MAR-001', 'Marcadores borrables x 8',               'Marcadores de tablero borrable, punta redonda, 8 colores.',      26700.00,    3, 'Pelikan',   NULL, 2, 1, NOW(6)),
  (8,  'ESC-PLA-001', 'Plastilina x 12 barras',                 'Plastilina no toxica en 12 colores.',                            11500.00,  150, 'Pelikan',   NULL, 2, 1, NOW(6)),
  (9,  'ESC-MOC-001', 'Morral escolar reforzado 18 pulgadas',   'Morral con espaldar acolchado y compartimento para portatil.',   89900.00,    0, 'Totto',     NULL, 2, 1, NOW(6)),
  (10, 'ESC-LON-001', 'Lonchera termica infantil',              'Lonchera con aislamiento termico y correa ajustable.',           37400.00,   28, 'Totto',     NULL, 2, 1, NOW(6)),

  (11, 'OFI-ARC-001', 'Archivador AZ carta lomo ancho',         'Archivador de palanca con rotulo intercambiable.',               14800.00,  310, 'Carvajal',  NULL, 3, 1, NOW(6)),
  (12, 'OFI-GRA-001', 'Grapadora metalica industrial',          'Grapadora de servicio pesado hasta 100 hojas.',                  68500.00,   19, 'Rapid',     NULL, 3, 1, NOW(6)),
  (13, 'OFI-ORG-001', 'Organizador de escritorio 5 niveles',    'Bandeja organizadora metalica para documentos.',                 72300.00,    2, 'Carvajal',  NULL, 3, 1, NOW(6)),
  (14, 'OFI-TAB-001', 'Tablero acrilico 120x90 cm',             'Tablero borrable con marco de aluminio y porta marcadores.',    189000.00,    7, 'Carvajal',  NULL, 3, 1, NOW(6)),

  (15, 'TEC-MOU-001', 'Mouse inalambrico ergonomico',           'Mouse optico 2.4GHz, 1600 DPI, bateria de larga duracion.',     54900.00,   41, 'Logitech',  NULL, 4, 1, NOW(6)),
  (16, 'TEC-BAS-001', 'Base refrigerante para portatil',        'Base con dos ventiladores silenciosos y altura regulable.',      79900.00,    0, 'Klip',      NULL, 4, 1, NOW(6)),
  (17, 'TEC-LAM-001', 'Lampara LED de escritorio regulable',    'Lampara con tres temperaturas de color y puerto USB.',           63400.00,   33, 'Klip',      NULL, 4, 1, NOW(6)),

  (18, 'LIB-NOV-001', 'Cien anos de soledad - edicion conmemorativa', 'Edicion de tapa dura con ilustraciones.',                 95000.00,   16, 'Diana',     NULL, 5, 1, NOW(6)),
  (19, 'LIB-INF-001', 'Coleccion cuentos infantiles x 6 tomos', 'Coleccion ilustrada de cuentos clasicos.',                      129900.00,    4, 'Norma',     NULL, 5, 1, NOW(6)),
  (20, 'LIB-TEC-001', 'Clean Code (edicion en espanol)',        'Manual de buenas practicas de desarrollo de software.',        142000.00,    9, 'Anaya',     NULL, 5, 1, NOW(6));

-- -----------------------------------------------------------------------------
-- Lista de deseos del cliente autenticado
-- -----------------------------------------------------------------------------
INSERT INTO wishlists (id, user_id, nombre, created_at) VALUES
  (1, 1, 'Mi lista de deseos', NOW(6));

-- -----------------------------------------------------------------------------
-- Items actuales de la lista.
-- Pensados para que el endpoint GET /api/v1/wishlist devuelva los tres estados:
--   producto 15 -> stock 41 y pide 1  => DISPONIBLE
--   producto 7  -> stock  3 y pide 5  => STOCK_INSUFICIENTE
--   producto 9  -> stock  0           => SIN_STOCK   (dispara la notificacion)
-- -----------------------------------------------------------------------------
INSERT INTO wishlist_items (id, wishlist_id, product_id, cantidad_deseada, prioridad, nota, created_at) VALUES
  (1, 1, 15, 1, 'MEDIA', 'Para reemplazar el mouse de la oficina', NOW(6)),
  (2, 1,  7, 5, 'ALTA',  'Para las capacitaciones del proximo mes', NOW(6)),
  (3, 1,  9, 2, 'ALTA',  'Regalo de cumpleanos',                    NOW(6)),
  (4, 1, 18, 1, 'BAJA',  NULL,                                      NOW(6));

-- -----------------------------------------------------------------------------
-- Historico.
-- Los cuatro eventos AGREGADO corresponden a los items que siguen en la lista.
-- El ultimo par de eventos demuestra el requisito 4: el producto 16 estuvo en la
-- lista, el cliente lo elimino, y el rastro de su paso por la lista permanece.
-- -----------------------------------------------------------------------------
INSERT INTO wishlist_item_history
    (wishlist_id, product_id, user_id, accion, cantidad_deseada, producto_nombre, producto_precio, stock_al_momento, occurred_at) VALUES
  (1, 15, 1, 'AGREGADO',    1, 'Mouse inalambrico ergonomico',         54900.00, 45, NOW(6) - INTERVAL 9 DAY),
  (1,  7, 1, 'AGREGADO',    2, 'Marcadores borrables x 8',             26700.00, 30, NOW(6) - INTERVAL 8 DAY),
  (1,  7, 1, 'ACTUALIZADO', 5, 'Marcadores borrables x 8',             26700.00, 12, NOW(6) - INTERVAL 5 DAY),
  (1,  9, 1, 'AGREGADO',    2, 'Morral escolar reforzado 18 pulgadas', 89900.00, 11, NOW(6) - INTERVAL 4 DAY),
  (1, 16, 1, 'AGREGADO',    1, 'Base refrigerante para portatil',      74900.00,  6, NOW(6) - INTERVAL 3 DAY),
  (1, 16, 1, 'ELIMINADO',   1, 'Base refrigerante para portatil',      74900.00,  0, NOW(6) - INTERVAL 2 DAY),
  (1, 18, 1, 'AGREGADO',    1, 'Cien anos de soledad - edicion conmemorativa', 95000.00, 20, NOW(6) - INTERVAL 1 DAY);
