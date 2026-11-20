-- ============================================================
-- DATOS MAESTROS — SIGVID (NEON)
-- Ejecutar manualmente en este orden:
--   0) arrancar el backend una vez (Hibernate crea las tablas con ddl-auto=update)
--   1) datos_maestros.sql
--   2) acceso_modulos.sql
--   3) catalogo_ventas_seed.sql
-- Es idempotente: ON CONFLICT DO NOTHING no duplica filas existentes.
-- IMPORTANTE: ON CONFLICT tambien ignora conflictos por "nombre" unico. Si la BD ya
-- tiene datos, verificar antes que los ids coincidan con los de este script:
--   SELECT id_rol, nombre FROM roles;  SELECT id_estado, nombre FROM estados_pedido;
-- ============================================================

-- 1. Roles (los ids se usan en acceso_modulos.sql)
INSERT INTO public.roles (id_rol, nombre, descripcion) VALUES
(1, 'Administrador', 'Acceso completo a la gestión del sistema'),
(2, 'Vendedor', 'Registra pedidos de venta y los deriva a almacén'),
(3, 'Cajero', 'Registra pagos y administra la caja'),
(4, 'Almacenero', 'Gestiona stock, asignación y despacho de pedidos'),
(5, 'Cliente', 'Consulta el catálogo, sus pedidos y confirma la recepción')
ON CONFLICT DO NOTHING;
SELECT setval('public.roles_id_rol_seq', GREATEST((SELECT MAX(id_rol) FROM public.roles), 1), true);

-- 2. Estados del pedido (flujo: Propuesto -> Pagado -> En preparación -> En tránsito -> Entregado)
INSERT INTO public.estados_pedido (id_estado, nombre, descripcion) VALUES
(1, 'Propuesto', 'Pedido registrado por el vendedor, pendiente de pago'),
(2, 'Pagado', 'Pago registrado por el cajero'),
(3, 'En preparación', 'Derivado a almacén para la asignación de productos'),
(4, 'En tránsito', 'Productos despachados desde almacén'),
(5, 'Entregado', 'Recepción confirmada por el cliente')
ON CONFLICT DO NOTHING;
SELECT setval('public.estados_pedido_id_estado_seq', GREATEST((SELECT MAX(id_estado) FROM public.estados_pedido), 1), true);

-- 3. Métodos de pago (HU05)
INSERT INTO public.metodos_pago (id_metodo, nombre) VALUES
(1, 'Efectivo'),
(2, 'Tarjeta'),
(3, 'Transferencia')
ON CONFLICT DO NOTHING;
SELECT setval('public.metodos_pago_id_metodo_seq', GREATEST((SELECT MAX(id_metodo) FROM public.metodos_pago), 1), true);

-- 4. Categorías (HU03; ids usados en catalogo_ventas_seed.sql)
INSERT INTO public.categorias (id_categoria, nombre) VALUES
(1, 'Vinos'),
(2, 'Piscos'),
(3, 'Corchos')
ON CONFLICT DO NOTHING;
SELECT setval('public.categorias_id_categoria_seq', GREATEST((SELECT MAX(id_categoria) FROM public.categorias), 1), true);

-- 5. Marcas (HU03; ids usados en catalogo_ventas_seed.sql)
INSERT INTO public.marcas (id_marca, nombre) VALUES
(1, 'Tacama'),
(2, 'Santiago Queirolo'),
(3, 'Tabernero'),
(4, 'Viejo Tonel')
ON CONFLICT DO NOTHING;
SELECT setval('public.marcas_id_marca_seq', GREATEST((SELECT MAX(id_marca) FROM public.marcas), 1), true);

-- 6. Sucursal principal (los pedidos y usuarios usan id_sucursal = 1 por defecto)
INSERT INTO public.sucursales (id_sucursal, nombre, direccion, telefono) VALUES
(1, 'Sede Principal Lima', 'Av. Javier Prado Este 1234, San Isidro, Lima', '014567890')
ON CONFLICT DO NOTHING;
SELECT setval('public.sucursales_id_sucursal_seq', GREATEST((SELECT MAX(id_sucursal) FROM public.sucursales), 1), true);
