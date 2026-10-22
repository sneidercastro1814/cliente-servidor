/**
 * Tipos que reflejan el contrato de la API REST.
 *
 * Se mantienen en un solo archivo para que el contrato entre cliente y
 * servidor quede en un sitio evidente: si cambia un DTO en Java, esto es lo
 * primero que hay que ajustar.
 */

export type EstadoStock = 'DISPONIBLE' | 'STOCK_INSUFICIENTE' | 'SIN_STOCK';
export type Prioridad = 'ALTA' | 'MEDIA' | 'BAJA';
export type AccionHistorial = 'AGREGADO' | 'ACTUALIZADO' | 'ELIMINADO';

export interface Categoria {
  id: number;
  nombre: string;
  slug: string;
  descripcion?: string;
}

export interface Producto {
  id: number;
  sku: string;
  nombre: string;
  descripcion?: string;
  precio: number;
  /** Cantidad existente: el requisito 1 pide mostrarla en el catalogo. */
  stock: number;
  disponible: boolean;
  marca?: string;
  imagenUrl?: string;
  categoria: Categoria;
}

/** Envoltura de paginacion propia de la API. */
export interface PaginaRespuesta<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
  primera: boolean;
  ultima: boolean;
}

export interface DeseoItem {
  id: number;
  producto: Producto;
  cantidadDeseada: number;
  prioridad: Prioridad;
  nota?: string;
  /** Lo calcula el servidor (requisito 3). */
  estadoStock: EstadoStock;
  mensajeStock: string;
  requiereNotificacion: boolean;
  agregadoEn?: string;
  actualizadoEn?: string;
}

export interface ListaDeseos {
  id: number;
  nombre: string;
  totalItems: number;
  itemsConNotificacion: number;
  notificaciones: string[];
  items: DeseoItem[];
}

export interface EventoHistorial {
  id: number;
  accion: AccionHistorial;
  productoId: number;
  /** Snapshot: el nombre que tenia el producto cuando ocurrio el evento. */
  productoNombre: string;
  productoPrecio: number;
  cantidadDeseada: number;
  stockAlMomento: number;
  fecha: string;
}

export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  rol: string;
}

export interface LoginRespuesta {
  token: string;
  tipo: string;
  expiraEnSegundos: number;
  usuario: Usuario;
}

/** Formato unico de error que devuelve la API. */
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  mensaje: string;
  path: string;
  detalles?: string[];
}

export interface AgregarDeseoPeticion {
  productoId: number;
  cantidadDeseada: number;
  prioridad?: Prioridad;
  nota?: string | null;
}

export interface ActualizarDeseoPeticion {
  cantidadDeseada: number;
  prioridad?: Prioridad;
  nota?: string | null;
}
