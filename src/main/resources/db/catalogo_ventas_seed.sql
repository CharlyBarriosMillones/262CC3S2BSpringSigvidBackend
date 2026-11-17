-- ============================================================
-- SEED DE CATÁLOGO Y CAJA PARA SPRINT 2 — SIGVID (NEON)
-- ============================================================

ALTER TABLE public.productos ADD COLUMN IF NOT EXISTS cepa VARCHAR(100);
ALTER TABLE public.productos ADD COLUMN IF NOT EXISTS descripcion TEXT;
ALTER TABLE public.productos ADD COLUMN IF NOT EXISTS maridaje TEXT;
ALTER TABLE public.productos ADD COLUMN IF NOT EXISTS imagen_url VARCHAR(255);
ALTER TABLE public.productos ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE;

-- 1. Proveedor base de Vitivinícolas Perú
INSERT INTO public.proveedores (id_proveedor, ruc, razon_social, contacto, telefono, email)
VALUES (1, '20556677881', 'Bodegas y Viñedos del Sur S.A.C.', 'Carlos Mendoza', '987654321', 'logistica@vitivinicolas.pe')
ON CONFLICT (id_proveedor) DO NOTHING;

SELECT setval('public.proveedores_id_proveedor_seq', 1, true);

-- 2. Caja Principal para HU05 (Cajero)
INSERT INTO public.cajas (id_caja, nombre, saldo_actual)
VALUES (1, 'Caja 1 - Principal', 1500.00)
ON CONFLICT (id_caja) DO NOTHING;

SELECT setval('public.cajas_id_caja_seq', 1, true);

-- 3. Productos del Catálogo (Vinos = cat 1, Piscos = cat 2, Corchos = cat 3)
INSERT INTO public.productos (id_producto, codigo_producto, nombre, precio_referencial, stock_minimo, id_categoria, id_marca, id_proveedor, cepa, descripcion, maridaje, imagen_url, activo) VALUES
(1, 'VIN-001', 'Gran Reserva Malbec', 85.00, 15, 1, 1, 1, 'Malbec 100%', 'Vino de color rojo rubí intenso. En nariz presenta aromas a frutos rojos maduros, ciruela y sutiles toques de vainilla aportados por su crianza en barrica de roble francés.', 'Ideal para acompañar carnes rojas a la parrilla, pastas con salsas intensas y quesos maduros.', 'images/vino_malbec.webp', true),
(2, 'VIN-002', 'Valle Sagrado Tannat', 95.50, 12, 1, 2, 1, 'Tannat 100%', 'Estructura firme y color profundo. Destacan notas de mora, chocolate negro y especias. Un vino con gran presencia en boca y final persistente.', 'Perfecto con cordero asado, carnes de caza y estofados de larga cocción.', 'images/vino_tannat.webp', true),
(3, 'VIN-003', 'Blanco de Blancos', 70.00, 20, 1, 3, 1, 'Chardonnay - Sauvignon Blanc', 'Fresco, elegante y vibrante. Resaltan notas cítricas, durazno blanco y toques minerales. Acidez equilibrada que aporta una frescura excepcional.', 'Excelente compañero de pescados, mariscos frescos, ceviche peruano y ensaladas de verano.', 'images/vino_blancos.webp', true),
(4, 'PIS-001', 'Pisco Puro Quebranta', 65.00, 25, 2, 1, 1, 'Uva Quebranta 100%', 'Destilado transparente, brillante y límpido. En nariz es sobrio con ligeros tonos a manzana y frutos secos. En boca es estructurado y con carácter.', 'Ideal para la coctelería clásica peruana como el Pisco Sour, o para acompañar postres tradicionales.', 'images/pisco_quebranta.webp', true),
(5, 'PIS-002', 'Pisco Mosto Verde Italia', 95.00, 15, 2, 2, 1, 'Uva Italia 100%', 'Elegante y aterciopelado. Al interrumpir la fermentación, conserva los azúcares naturales que potencian sus aromas cítricos, florales y notas a jazmín y lima.', 'Perfecto para tomar puro como digestivo frío, o acompañando postres a base de frutas frescas.', 'images/pisco_italia.webp', true),
(6, 'PIS-003', 'Pisco Acholado Premium', 75.00, 20, 2, 4, 1, 'Blend: Quebranta, Italia y Torontel', 'Un ensamblaje perfecto que combina la estructura de la Quebranta con los aromas exuberantes de las uvas aromáticas. Equilibrado, redondo y muy versátil.', 'Excelente para Chilcanos y coctelería de autor. Acompaña muy bien piqueos marinos y tiraditos.', 'images/pisco_acholado.webp', true),
(7, 'COR-001', 'Corcho Natural Flor', 1.50, 500, 3, 1, 1, '100% Alcornoque Natural', 'Extraído de la corteza del alcornoque con el máximo grado de pureza. Su elasticidad y permeabilidad controlada permiten una microoxigenación perfecta.', 'Exclusivo para vinos tintos de larga guarda (más de 5 años en botella).', 'images/corcho_natural_flor.webp', true),
(8, 'COR-002', 'Microaglomerado Premium', 0.85, 800, 3, 3, 1, 'Gránulos de corcho natural', 'Fabricado a partir de gránulos de corcho esterilizados y moldeados. Garantiza una homogeneidad absoluta botella tras botella.', 'Ideal para vinos blancos, rosados y tintos jóvenes de consumo rápido (1 a 3 años).', 'images/corcho_microaglomerado.webp', true),
(9, 'COR-003', 'Tapón Cabezudo (T-Cork)', 1.20, 600, 3, 4, 1, 'Cuerpo natural con tapa de madera', 'Diseño ergonómico que permite un descorche manual sin necesidad de sacacorchos. Asegura un sellado hermético que protege los destilados.', 'Diseñado específicamente para nuestra línea de Piscos y licores premium.', 'images/corcho_cabezudo.webp', true)
ON CONFLICT (id_producto) DO NOTHING;

SELECT setval('public.productos_id_producto_seq', 9, true);