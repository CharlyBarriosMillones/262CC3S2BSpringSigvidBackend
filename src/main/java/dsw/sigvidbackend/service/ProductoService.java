package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.ProductoRequest;
import dsw.sigvidbackend.dto.ProductoResponse;
import dsw.sigvidbackend.model.Categoria;
import dsw.sigvidbackend.model.Marca;
import dsw.sigvidbackend.model.Producto;
import dsw.sigvidbackend.model.Proveedor;
import dsw.sigvidbackend.repository.CategoriaRepository;
import dsw.sigvidbackend.repository.MarcaRepository;
import dsw.sigvidbackend.repository.ProductoRepository;
import dsw.sigvidbackend.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final ProveedorRepository proveedorRepository;

    @Transactional
    public List<ProductoResponse> listarConFiltros(Long idCategoria, Long idMarca, String busqueda) {
        if (productoRepository.count() == 0) {
            inicializarCatalogoPorDefecto();
        }

        String termino = (busqueda != null && !busqueda.isBlank()) ? busqueda.trim().toLowerCase() : null;

        List<Producto> filtrados = productoRepository.findAllByOrderByIdProductoAsc().stream()
                .filter(p -> p.getActivo() == null || p.getActivo())
                .filter(p -> idCategoria == null || (p.getCategoria() != null && idCategoria.equals(p.getCategoria().getIdCategoria())))
                .filter(p -> idMarca == null || (p.getMarca() != null && idMarca.equals(p.getMarca().getIdMarca())))
                .filter(p -> termino == null
                        || (p.getNombre() != null && p.getNombre().toLowerCase().contains(termino))
                        || (p.getCodigoProducto() != null && p.getCodigoProducto().toLowerCase().contains(termino)))
                .collect(Collectors.toList());

        return ProductoResponse.fromEntities(filtrados);
    }

    public ProductoResponse obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado con ID: " + id));
        return ProductoResponse.fromEntity(producto);
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    public List<Marca> listarMarcas() {
        return marcaRepository.findAll();
    }

    @Transactional
    public ProductoResponse guardarProducto(ProductoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new NoSuchElementException("Categoría no encontrada con ID: " + request.getIdCategoria()));

        Marca marca = marcaRepository.findById(request.getIdMarca())
                .orElseThrow(() -> new NoSuchElementException("Marca no encontrada con ID: " + request.getIdMarca()));

        Proveedor proveedor = obtenerOCrearProveedorBase();

        Producto producto;
        if (request.getIdProducto() != null && request.getIdProducto() > 0) {
            producto = productoRepository.findById(request.getIdProducto())
                    .orElseThrow(() -> new NoSuchElementException("Producto no encontrado con ID: " + request.getIdProducto()));
        } else {
            if (productoRepository.existsByCodigoProducto(request.getCodigoProducto().trim())) {
                throw new IllegalArgumentException("Ya existe un producto con el código: " + request.getCodigoProducto());
            }
            producto = new Producto();
        }

        producto.setCodigoProducto(request.getCodigoProducto().trim());
        producto.setNombre(request.getNombre().trim());
        producto.setPrecioReferencial(request.getPrecioReferencial());
        producto.setStockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : 10);
        producto.setCategoria(categoria);
        producto.setMarca(marca);
        producto.setProveedor(proveedor);
        producto.setCepa(request.getCepa());
        producto.setDescripcion(request.getDescripcion());
        producto.setMaridaje(request.getMaridaje());
        producto.setImagenUrl(request.getImagenUrl());
        producto.setActivo(request.getActivo() != null ? request.getActivo() : true);

        producto = productoRepository.saveAndFlush(producto);
        return ProductoResponse.fromEntity(producto);
    }

    private Proveedor obtenerOCrearProveedorBase() {
        return proveedorRepository.findAll().stream().findFirst()
                .orElseGet(() -> proveedorRepository.saveAndFlush(
                        Proveedor.builder()
                                .ruc("20556677881")
                                .razonSocial("Bodegas y Viñedos del Sur S.A.C.")
                                .contacto("Carlos Mendoza")
                                .telefono("987654321")
                                .email("logistica@vitivinicolas.pe")
                                .build()
                ));
    }

    private void inicializarCatalogoPorDefecto() {
        Categoria catVinos = categoriaRepository.findByNombreIgnoreCase("Vinos")
                .orElseGet(() -> categoriaRepository.saveAndFlush(Categoria.builder().nombre("Vinos").build()));
        Categoria catPiscos = categoriaRepository.findByNombreIgnoreCase("Piscos")
                .orElseGet(() -> categoriaRepository.saveAndFlush(Categoria.builder().nombre("Piscos").build()));
        Categoria catCorchos = categoriaRepository.findByNombreIgnoreCase("Corchos")
                .orElseGet(() -> categoriaRepository.saveAndFlush(Categoria.builder().nombre("Corchos").build()));

        Marca mTacama = marcaRepository.findByNombreIgnoreCase("Tacama")
                .orElseGet(() -> marcaRepository.saveAndFlush(Marca.builder().nombre("Tacama").build()));
        Marca mQueirolo = marcaRepository.findByNombreIgnoreCase("Santiago Queirolo")
                .orElseGet(() -> marcaRepository.saveAndFlush(Marca.builder().nombre("Santiago Queirolo").build()));
        Marca mTabernero = marcaRepository.findByNombreIgnoreCase("Tabernero")
                .orElseGet(() -> marcaRepository.saveAndFlush(Marca.builder().nombre("Tabernero").build()));
        Marca mViejoTonel = marcaRepository.findByNombreIgnoreCase("Viejo Tonel")
                .orElseGet(() -> marcaRepository.saveAndFlush(Marca.builder().nombre("Viejo Tonel").build()));

        Proveedor prov = obtenerOCrearProveedorBase();

        List<Producto> semillas = List.of(
                Producto.builder().codigoProducto("VIN-001").nombre("Gran Reserva Malbec").precioReferencial(new BigDecimal("85.00")).stockMinimo(15).categoria(catVinos).marca(mTacama).proveedor(prov).cepa("Malbec 100%").descripcion("Vino de color rojo rubí intenso con aromas a frutos rojos maduros, ciruela y vainilla.").maridaje("Carnes rojas a la parrilla y quesos maduros.").imagenUrl("images/vino_malbec.webp").activo(true).build(),
                Producto.builder().codigoProducto("VIN-002").nombre("Valle Sagrado Tannat").precioReferencial(new BigDecimal("95.50")).stockMinimo(12).categoria(catVinos).marca(mQueirolo).proveedor(prov).cepa("Tannat 100%").descripcion("Estructura firme y color profundo. Notas de mora, chocolate negro y especias.").maridaje("Cordero asado y estofados de larga cocción.").imagenUrl("images/vino_tannat.webp").activo(true).build(),
                Producto.builder().codigoProducto("VIN-003").nombre("Blanco de Blancos").precioReferencial(new BigDecimal("70.00")).stockMinimo(20).categoria(catVinos).marca(mTabernero).proveedor(prov).cepa("Chardonnay - Sauvignon Blanc").descripcion("Fresco, elegante y vibrante con notas cítricas y durazno blanco.").maridaje("Pescados, mariscos frescos y ceviche peruano.").imagenUrl("images/vino_blancos.webp").activo(true).build(),
                Producto.builder().codigoProducto("PIS-001").nombre("Pisco Puro Quebranta").precioReferencial(new BigDecimal("65.00")).stockMinimo(25).categoria(catPiscos).marca(mTacama).proveedor(prov).cepa("Uva Quebranta 100%").descripcion("Destilado transparente y brillante con tonos a manzana y frutos secos.").maridaje("Ideal para Pisco Sour y postres tradicionales.").imagenUrl("images/pisco_quebranta.webp").activo(true).build(),
                Producto.builder().codigoProducto("PIS-002").nombre("Pisco Mosto Verde Italia").precioReferencial(new BigDecimal("95.00")).stockMinimo(15).categoria(catPiscos).marca(mQueirolo).proveedor(prov).cepa("Uva Italia 100%").descripcion("Elegante y aterciopelado con aromas cítricos, jazmín y lima.").maridaje("Digestivo frío o postres de frutas frescas.").imagenUrl("images/pisco_italia.webp").activo(true).build(),
                Producto.builder().codigoProducto("PIS-003").nombre("Pisco Acholado Premium").precioReferencial(new BigDecimal("75.00")).stockMinimo(20).categoria(catPiscos).marca(mViejoTonel).proveedor(prov).cepa("Blend: Quebranta, Italia y Torontel").descripcion("Ensamblaje equilibrado que combina estructura y aromas exuberantes.").maridaje("Excelente para Chilcanos y tiraditos.").imagenUrl("images/pisco_acholado.webp").activo(true).build(),
                Producto.builder().codigoProducto("COR-001").nombre("Corcho Natural Flor").precioReferencial(new BigDecimal("1.50")).stockMinimo(500).categoria(catCorchos).marca(mTacama).proveedor(prov).cepa("100% Alcornoque Natural").descripcion("Máximo grado de pureza y microoxigenación controlada para larga guarda.").maridaje("Vinos tintos Gran Reserva.").imagenUrl("images/corcho_natural_flor.webp").activo(true).build(),
                Producto.builder().codigoProducto("COR-002").nombre("Microaglomerado Premium").precioReferencial(new BigDecimal("0.85")).stockMinimo(800).categoria(catCorchos).marca(mTabernero).proveedor(prov).cepa("Gránulos de corcho natural").descripcion("Gránulos esterilizados y moldeados que garantizan homogeneidad absoluta.").maridaje("Vinos blancos, rosados y tintos jóvenes.").imagenUrl("images/corcho_microaglomerado.webp").activo(true).build(),
                Producto.builder().codigoProducto("COR-003").nombre("Tapón Cabezudo (T-Cork)").precioReferencial(new BigDecimal("1.20")).stockMinimo(600).categoria(catCorchos).marca(mViejoTonel).proveedor(prov).cepa("Cuerpo natural con tapa de madera").descripcion("Diseño ergonómico de descorche manual y sellado hermético.").maridaje("Línea de Piscos y destilados premium.").imagenUrl("images/corcho_cabezudo.webp").activo(true).build()
        );

        productoRepository.saveAllAndFlush(semillas);
    }
}