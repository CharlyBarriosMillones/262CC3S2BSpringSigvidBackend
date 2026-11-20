-- ============================================================
-- SEED SPRINT 3 — ALMACÉN Y LOGÍSTICA (HU07 a HU10) — SIGVID (NEON)
-- Ejecutar después de: datos_maestros.sql, acceso_modulos.sql y catalogo_ventas_seed.sql
-- (las tablas ubigeo, almacenes, stock, guias_despacho, detalles_guia y
--  recepciones_pedido las crea Hibernate al arrancar el backend).
-- Idempotente: ON CONFLICT DO NOTHING.
-- ============================================================

-- 1. Ubigeos de las sedes (código INEI de distrito, igual al formato del proyecto base)
INSERT INTO public.ubigeo (idubigeo, departamento, provincia, distrito) VALUES
('150101', 'LIMA', 'LIMA', 'LIMA'),
('150131', 'LIMA', 'LIMA', 'SAN ISIDRO'),
('150103', 'LIMA', 'LIMA', 'ATE'),
('110101', 'ICA', 'ICA', 'ICA'),
('110501', 'ICA', 'PISCO', 'PISCO'),
('040101', 'AREQUIPA', 'AREQUIPA', 'AREQUIPA')
ON CONFLICT DO NOTHING;

-- 2. Almacenes de la empresa
INSERT INTO public.almacenes (id_almacen, nombre, direccion, idubigeo, activo) VALUES
(1, 'Almacén Central Lima', 'Av. Nicolás Ayllón 2550, Ate', '150103', true),
(2, 'Almacén Bodega Ica', 'Carretera Panamericana Sur Km 300, Ica', '110101', true),
(3, 'Almacén Pisco', 'Calle San Francisco 120, Pisco', '110501', true)
ON CONFLICT DO NOTHING;
SELECT setval('public.almacenes_id_almacen_seq', GREATEST((SELECT MAX(id_almacen) FROM public.almacenes), 1), true);

-- 3. Stock inicial (producto x almacén). Incluye casos OK, Bajo y Crítico para HU07.
--    stock_minimo de productos: VIN-001=15, VIN-002=12, VIN-003=20, PIS-001=25, PIS-002=15,
--    PIS-003=20, COR-001=500, COR-002=800, COR-003=600
INSERT INTO public.stock (id_producto, id_almacen, cantidad_actual) VALUES
(1, 1, 120), (2, 1, 10),  (3, 1, 8),   (4, 1, 200), (5, 1, 40),  (6, 1, 22),
(7, 1, 3000), (8, 1, 350), (9, 1, 900),
(1, 2, 60),  (2, 2, 45),  (3, 2, 30),  (4, 2, 12),  (5, 2, 7),   (6, 2, 80),
(4, 3, 150), (5, 3, 90),  (6, 3, 18),  (9, 3, 250)
ON CONFLICT DO NOTHING;

-- 4. Módulos del Sprint 3 en el menú dinámico (complementa acceso_modulos.sql)
UPDATE public.modulos
   SET nombre = 'Consultar Stock',
       descripcion = 'Consulta el stock actual, mínimo y estado por almacén y ubicación.'
 WHERE id_mod = 6;

INSERT INTO public.modulos (id_mod, nombre, descripcion, url, icono, activo, orden_opcion) VALUES
(10, 'Asignar Productos a Pedido', 'Asigna productos a pedidos derivados y genera la guía de despacho.', '/principal/asignar-pedido', 'bi-clipboard-check', true, 10),
(11, 'Registrar Salida (QR)', 'Muestra el QR de cada producto, registra el escaneo y confirma la salida.', '/principal/registrar-salida', 'bi-qr-code', true, 11),
(12, 'Confirmar Recepción', 'Confirma la recepción de tus pedidos con conformidad o firma digital.', '/principal/confirmar-recepcion', 'bi-pen', true, 12),
(13, 'Dashboard de Indicadores', 'Ventas diarias, stock por categoría y pedidos por estado.', '/principal/dashboard', 'bi-graph-up-arrow', true, 13)
ON CONFLICT DO NOTHING;
SELECT setval('public.modulos_id_mod_seq', GREATEST((SELECT MAX(id_mod) FROM public.modulos), 1), true);

-- Administrador (1) y Almacenero (4): HU08 y HU09. Cliente (5): HU10. Dashboard: Admin (1) y Vendedor (2).
INSERT INTO public.rol_modulo (id_rol, id_mod, activo) VALUES
(1, 10, true), (1, 11, true), (1, 13, true), (2, 13, true),
(4, 10, true), (4, 11, true),
(5, 12, true)
ON CONFLICT DO NOTHING;
