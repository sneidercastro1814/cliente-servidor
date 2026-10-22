-- =============================================================================
-- Prueba Tecnica Carvajal - Lista de Deseos
-- V1: Esquema inicial
--
-- Motor:      MySQL 8 / InnoDB
-- Charset:    utf8mb4 (soporta tildes, enies y emojis)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- users
-- La prueba dice "asuma que el usuario ya existe y se encuentra autenticado",
-- pero necesitamos la tabla por dos razones: la lista de deseos pertenece a un
-- usuario (integridad referencial) y el plus de JWT requiere validar credenciales.
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(120) NOT NULL,
    email         VARCHAR(180) NOT NULL,
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt',
    rol           VARCHAR(20)  NOT NULL DEFAULT 'CLIENTE',
    activo        TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NULL,
    CONSTRAINT pk_users       PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- -----------------------------------------------------------------------------
-- categories
-- Propiedad agregada para "complementar la aplicacion": permite filtrar el
-- catalogo, que es el primer requisito funcional.
-- -----------------------------------------------------------------------------
CREATE TABLE categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(120) NOT NULL,
    slug        VARCHAR(140) NOT NULL,
    descripcion VARCHAR(500) NULL,
    CONSTRAINT pk_categories      PRIMARY KEY (id),
    CONSTRAINT uk_categories_slug UNIQUE (slug)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- -----------------------------------------------------------------------------
-- products
-- La prueba exige: ID, nombre, precio y cantidad en stock. Se agregan sku,
-- descripcion, marca, imagen y categoria para completar un catalogo realista.
-- -----------------------------------------------------------------------------
CREATE TABLE products (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    sku         VARCHAR(40)   NOT NULL,
    nombre      VARCHAR(180)  NOT NULL,
    descripcion VARCHAR(1000) NULL,
    precio      DECIMAL(12, 2) NOT NULL COMMENT 'COP',
    stock       INT           NOT NULL DEFAULT 0 COMMENT 'Cantidad existente',
    marca       VARCHAR(120)  NULL,
    imagen_url  VARCHAR(500)  NULL,
    category_id BIGINT        NOT NULL,
    activo      TINYINT(1)    NOT NULL DEFAULT 1,
    created_at  DATETIME(6)   NOT NULL,
    updated_at  DATETIME(6)   NULL,
    CONSTRAINT pk_products          PRIMARY KEY (id),
    CONSTRAINT uk_products_sku      UNIQUE (sku),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT ck_products_precio   CHECK (precio >= 0),
    CONSTRAINT ck_products_stock    CHECK (stock >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX ix_products_nombre   ON products (nombre);
CREATE INDEX ix_products_category ON products (category_id);
CREATE INDEX ix_products_activo   ON products (activo);

-- -----------------------------------------------------------------------------
-- wishlists
-- Una lista por usuario (uk_wishlists_user). Se modela como entidad propia y no
-- como una simple columna en users para poder colgarle metadatos y para dejar
-- abierta la posibilidad de varias listas por cliente mas adelante.
-- -----------------------------------------------------------------------------
CREATE TABLE wishlists (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    nombre     VARCHAR(120) NOT NULL DEFAULT 'Mi lista de deseos',
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NULL,
    CONSTRAINT pk_wishlists      PRIMARY KEY (id),
    CONSTRAINT uk_wishlists_user UNIQUE (user_id),
    CONSTRAINT fk_wishlists_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- -----------------------------------------------------------------------------
-- wishlist_items
-- Estado ACTUAL de la lista. La restriccion uk_wishlist_items_producto impide
-- que el mismo producto se repita en una lista: eso convierte "agregar un
-- producto que ya esta" en un error de negocio controlado y no en un duplicado.
-- -----------------------------------------------------------------------------
CREATE TABLE wishlist_items (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    wishlist_id      BIGINT       NOT NULL,
    product_id       BIGINT       NOT NULL,
    cantidad_deseada INT          NOT NULL DEFAULT 1,
    prioridad        VARCHAR(10)  NOT NULL DEFAULT 'MEDIA',
    nota             VARCHAR(300) NULL,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NULL,
    CONSTRAINT pk_wishlist_items          PRIMARY KEY (id),
    CONSTRAINT uk_wishlist_items_producto UNIQUE (wishlist_id, product_id),
    CONSTRAINT fk_wishlist_items_wishlist FOREIGN KEY (wishlist_id) REFERENCES wishlists (id),
    CONSTRAINT fk_wishlist_items_product  FOREIGN KEY (product_id)  REFERENCES products (id),
    CONSTRAINT ck_wishlist_items_cantidad CHECK (cantidad_deseada > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX ix_wishlist_items_wishlist ON wishlist_items (wishlist_id);

-- -----------------------------------------------------------------------------
-- wishlist_item_history
-- Requisito funcional 4: "debe almacenarse el historico de registros que hayan
-- llegado a la lista de deseos".
--
-- Decision de diseno: tabla de EVENTOS append-only, separada del estado actual.
--   * Si el cliente elimina un deseo, el item desaparece de wishlist_items pero
--     el rastro queda aqui. Un borrado logico sobre wishlist_items no serviria:
--     perderia el historial de cambios de cantidad y romperia la restriccion
--     de unicidad al volver a agregar el mismo producto.
--   * Guardamos un snapshot de nombre, precio y stock del producto en el momento
--     del evento, para que el historico no se altere si el producto cambia despues.
--   * Las llaves foraneas NO tienen ON DELETE CASCADE: el historico es inmutable.
-- -----------------------------------------------------------------------------
CREATE TABLE wishlist_item_history (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    wishlist_id      BIGINT         NOT NULL,
    product_id       BIGINT         NOT NULL,
    user_id          BIGINT         NOT NULL,
    accion           VARCHAR(20)    NOT NULL COMMENT 'AGREGADO | ACTUALIZADO | ELIMINADO',
    cantidad_deseada INT            NOT NULL,
    producto_nombre  VARCHAR(180)   NOT NULL COMMENT 'Snapshot',
    producto_precio  DECIMAL(12, 2) NOT NULL COMMENT 'Snapshot',
    stock_al_momento INT            NOT NULL COMMENT 'Snapshot',
    occurred_at      DATETIME(6)    NOT NULL,
    CONSTRAINT pk_wishlist_item_history          PRIMARY KEY (id),
    CONSTRAINT fk_wishlist_item_history_wishlist FOREIGN KEY (wishlist_id) REFERENCES wishlists (id),
    CONSTRAINT fk_wishlist_item_history_product  FOREIGN KEY (product_id)  REFERENCES products (id),
    CONSTRAINT fk_wishlist_item_history_user     FOREIGN KEY (user_id)     REFERENCES users (id),
    CONSTRAINT ck_wishlist_item_history_cantidad CHECK (cantidad_deseada > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX ix_wih_wishlist_fecha ON wishlist_item_history (wishlist_id, occurred_at);
CREATE INDEX ix_wih_producto       ON wishlist_item_history (product_id);
