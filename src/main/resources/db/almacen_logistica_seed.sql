-- ============================================================
-- SEED SPRINT 3 — ALMACÉN, LOGÍSTICA Y DASHBOARD INICIAL (NEON)
-- ============================================================

-- 0. Asegurar que exista la Sucursal 1 y compatibilizar columnas de BD.sql previo
INSERT INTO public.sucursales (id_sucursal, nombre, direccion, telefono) VALUES
(1, 'Sede Principal Lima', 'Av. Javier Prado Este 1234, San Isidro, Lima', '014567890')
ON CONFLICT DO NOTHING;

ALTER TABLE public.almacenes ADD COLUMN IF NOT EXISTS id_sucursal BIGINT DEFAULT 1;
ALTER TABLE public.almacenes ALTER COLUMN id_sucursal SET DEFAULT 1;
ALTER TABLE public.almacenes ALTER COLUMN id_sucursal DROP NOT NULL;

DO $$
DECLARE
    col RECORD;
BEGIN
    -- Flexibilizar cualquier columna extra NOT NULL heredada en almacenes o stock
    FOR col IN
        SELECT table_name, column_name
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND (
              (table_name = 'almacenes' AND column_name NOT IN ('id_almacen', 'nombre', 'idubigeo'))
              OR (table_name = 'stock' AND column_name NOT IN ('id_stock', 'id_producto', 'id_almacen', 'cantidad_actual'))
          )
          AND is_nullable = 'NO'
    LOOP
        EXECUTE format('ALTER TABLE public.%I ALTER COLUMN %I DROP NOT NULL', col.table_name, col.column_name);
    END LOOP;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_stock_producto_almacen
    ON public.stock (id_producto, id_almacen);

-- 1. Ubigeos de las sedes
INSERT INTO public.ubigeo (idubigeo, departamento, provincia, distrito) VALUES
('150101', 'LIMA', 'LIMA', 'LIMA'),
('150131', 'LIMA', 'LIMA', 'SAN ISIDRO'),
('150103', 'LIMA', 'LIMA', 'ATE'),
('110101', 'ICA', 'ICA', 'ICA'),
('110501', 'ICA', 'PISCO', 'PISCO'),
('040101', 'AREQUIPA', 'AREQUIPA', 'AREQUIPA')
ON CONFLICT DO NOTHING;

-- 2. Almacenes de la empresa (con id_sucursal = 1)
INSERT INTO public.almacenes (id_almacen, id_sucursal, nombre, direccion, idubigeo, activo) VALUES
(1, 1, 'Almacén Central Lima', 'Av. Nicolás Ayllón 2550, Ate', '150103', true),
(2, 1, 'Almacén Bodega Ica', 'Carretera Panamericana Sur Km 300, Ica', '110101', true),
(3, 1, 'Almacén Pisco', 'Calle San Francisco 120, Pisco', '110501', true)
ON CONFLICT DO NOTHING;
SELECT setval('public.almacenes_id_almacen_seq', GREATEST((SELECT MAX(id_almacen) FROM public.almacenes), 1), true);

-- 3. Stock inicial (incluye estados OK, Bajo y Crítico para HU07 y Radar Chart)
INSERT INTO public.stock (id_producto, id_almacen, cantidad_actual) VALUES
(1, 1, 120), (2, 1, 10),  (3, 1, 8),   (4, 1, 200), (5, 1, 40),  (6, 1, 22),
(7, 1, 3000), (8, 1, 350), (9, 1, 900),
(1, 2, 60),  (2, 2, 45),  (3, 2, 30),  (4, 2, 12),  (5, 2, 7),   (6, 2, 80),
(4, 3, 150), (5, 3, 90),  (6, 3, 18),  (9, 3, 250)
ON CONFLICT DO NOTHING;

-- 4. Módulos del Sprint 3 y Dashboard en orden_opcion = 1 (pantalla inicial)
UPDATE public.modulos
   SET nombre = 'Consultar Stock',
       descripcion = 'Consulta el stock actual, mínimo y estado por almacén y ubicación.'
 WHERE id_mod = 6;

UPDATE public.modulos SET orden_opcion = 2  WHERE id_mod = 1;
UPDATE public.modulos SET orden_opcion = 3  WHERE id_mod = 2;
UPDATE public.modulos SET orden_opcion = 4  WHERE id_mod = 3;
UPDATE public.modulos SET orden_opcion = 5  WHERE id_mod = 4;
UPDATE public.modulos SET orden_opcion = 6  WHERE id_mod = 5;
UPDATE public.modulos SET orden_opcion = 7  WHERE id_mod = 6;
UPDATE public.modulos SET orden_opcion = 8  WHERE id_mod = 7;
UPDATE public.modulos SET orden_opcion = 9  WHERE id_mod = 8;
UPDATE public.modulos SET orden_opcion = 10 WHERE id_mod = 9;

INSERT INTO public.modulos (id_mod, nombre, descripcion, url, icono, activo, orden_opcion) VALUES
(10, 'Asignar Productos a Pedido', 'Asigna productos a pedidos derivados y genera la guía de despacho.', '/principal/asignar-pedido', 'bi-clipboard-check', true, 11),
(11, 'Registrar Salida (QR)', 'Muestra el QR de cada producto, registra el escaneo y confirma la salida.', '/principal/registrar-salida', 'bi-qr-code', true, 12),
(12, 'Confirmar Recepción', 'Confirma la recepción de tus pedidos con conformidad o firma digital.', '/principal/confirmar-recepcion', 'bi-pen', true, 13),
(13, 'Dashboard de Indicadores', 'Ventas diarias, stock por categoría y pedidos por estado.', '/principal/dashboard', 'bi-graph-up-arrow', true, 1)
ON CONFLICT (id_mod) DO UPDATE SET orden_opcion = EXCLUDED.orden_opcion;

SELECT setval('public.modulos_id_mod_seq', GREATEST((SELECT MAX(id_mod) FROM public.modulos), 1), true);

-- 5. Asignación de módulos a roles (Dashboard habilitado para los 5 roles)
INSERT INTO public.rol_modulo (id_rol, id_mod, activo) VALUES
(1, 10, true), (1, 11, true), (1, 13, true),
(2, 13, true),
(3, 13, true),
(4, 10, true), (4, 11, true), (4, 13, true),
(5, 12, true), (5, 13, true)
ON CONFLICT DO NOTHING;