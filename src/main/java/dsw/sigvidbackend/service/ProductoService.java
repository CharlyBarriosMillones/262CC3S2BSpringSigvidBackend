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

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final ProveedorRepository proveedorRepository;

    public List<ProductoResponse> listarConFiltros(Long idCategoria, Long idMarca, String busqueda) {
        String termino = (busqueda != null && !busqueda.isBlank()) ? busqueda.trim() : null;
        return ProductoResponse.fromEntities(
                productoRepository.buscarConFiltros(idCategoria, idMarca, termino)
        );
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

        Long idProv = request.getIdProveedor() != null ? request.getIdProveedor() : 1L;
        Proveedor proveedor = proveedorRepository.findById(idProv)
                .orElseGet(() -> proveedorRepository.save(
                        Proveedor.builder()
                                .ruc("20556677881")
                                .razonSocial("Bodegas y Viñedos del Sur S.A.C.")
                                .contacto("Carlos Mendoza")
                                .telefono("987654321")
                                .email("logistica@vitivinicolas.pe")
                                .build()
                ));

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
}