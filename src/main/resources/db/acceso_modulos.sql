-- ============================================================
-- MÓDULO DE ACCESO Y MENÚ DINÁMICO POR ROL — SIGVID (NEON)
-- ============================================================

CREATE TABLE IF NOT EXISTS public.modulos (
    id_mod SERIAL PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE,
    descripcion VARCHAR(200) NOT NULL,
    url VARCHAR(120) NOT NULL,
    icono VARCHAR(60) NOT NULL DEFAULT 'bi-grid-fill',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    orden_opcion INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS public.rol_modulo (
    id_rol BIGINT NOT NULL REFERENCES public.roles(id_rol) ON DELETE CASCADE,
    id_mod INTEGER NOT NULL REFERENCES public.modulos(id_mod) ON DELETE CASCADE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_rol, id_mod)
);

-- Insertar Módulos del Sistema SIGVID (Sprints 1 y 2)
INSERT INTO public.modulos (id_mod, nombre, descripcion, url, icono, activo, orden_opcion) VALUES
(1, 'Mi Perfil y Preferencias', 'Edita tus datos básicos, foto de perfil y notificaciones por email.', '/principal/perfil', 'bi-person-lines-fill', true, 1),
(2, 'Catálogo Vitivinícola', 'Explora vinos, piscos y corchos con filtros por categoría y marca.', '/principal/catalogo', 'bi-collection-fill', true, 2),
(3, 'Registrar Pedido de Venta', 'Genera pedidos de clientes con cálculo automático y factura SUNAT.', '/principal/registrar-pedido', 'bi-cart-plus-fill', true, 3),
(4, 'Derivar Pedido a Almacén', 'Revisa pedidos pagados y derívalos a almacén para su preparación.', '/principal/derivar-pedido', 'bi-box-arrow-in-right', true, 4),
(5, 'Registrar Pago y Caja', 'Registra cobros en efectivo, tarjeta o transferencia con voucher digital.', '/principal/registrar-pago', 'bi-cash-coin', true, 5),
(6, 'Control de Stock y Despacho', 'Consulta disponibilidad de inventario por almacén y prepara pedidos.', '/principal/stock-almacen', 'bi-boxes', true, 6),
(7, 'Mis Pedidos y Cuenta', 'Consulta el historial de tus compras, estados de entrega y comprobantes.', '/principal/mis-pedidos', 'bi-bag-heart-fill', true, 7),
(8, 'Usuarios y Personal', 'Administra cuentas de vendedores, cajeros, almaceneros y clientes.', '/principal/usuarios', 'bi-people-fill', true, 8),
(9, 'Sucursales y Roles', 'Configura las sedes operativas y permisos por rol del sistema.', '/principal/sucursales', 'bi-building-fill-gear', true, 9)
ON CONFLICT (id_mod) DO NOTHING;

SELECT setval('public.modulos_id_mod_seq', 9, true);

-- Asignación de Módulos por Rol:
-- Rol 1: Administrador (acceso completo a gestión, ventas, caja y perfil)
INSERT INTO public.rol_modulo (id_rol, id_mod, activo) VALUES
(1, 1, true), (1, 2, true), (1, 3, true), (1, 4, true), (1, 5, true), (1, 6, true), (1, 8, true), (1, 9, true)
ON CONFLICT DO NOTHING;

-- Rol 2: Vendedor (Perfil, Catálogo, Registrar Pedido HU04, Derivar Pedido HU06)
INSERT INTO public.rol_modulo (id_rol, id_mod, activo) VALUES
(2, 1, true), (2, 2, true), (2, 3, true), (2, 4, true)
ON CONFLICT DO NOTHING;

-- Rol 3: Cajero (Perfil, Catálogo, Registrar Pago y Caja HU05)
INSERT INTO public.rol_modulo (id_rol, id_mod, activo) VALUES
(3, 1, true), (3, 2, true), (3, 5, true)
ON CONFLICT DO NOTHING;

-- Rol 4: Almacenero (Perfil, Catálogo, Control de Stock y Despacho)
INSERT INTO public.rol_modulo (id_rol, id_mod, activo) VALUES
(4, 1, true), (4, 2, true), (4, 6, true)
ON CONFLICT DO NOTHING;

-- Rol 5: Cliente (Perfil HU02, Catálogo HU03, Mis Pedidos HU02)
INSERT INTO public.rol_modulo (id_rol, id_mod, activo) VALUES
(5, 1, true), (5, 2, true), (5, 7, true)
ON CONFLICT DO NOTHING;