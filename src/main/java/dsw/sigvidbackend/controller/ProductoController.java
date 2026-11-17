package dsw.sigvidbackend.controller;

import dsw.sigvidbackend.dto.ProductoRequest;
import dsw.sigvidbackend.dto.ProductoResponse;
import dsw.sigvidbackend.model.Categoria;
import dsw.sigvidbackend.model.Marca;
import dsw.sigvidbackend.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);
    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listarProductos(
            @RequestParam(required = false) Long idCategoria,
            @RequestParam(required = false) Long idMarca,
            @RequestParam(required = false) String busqueda) {
        return ResponseEntity.ok(productoService.listarConFiltros(idCategoria, idMarca, busqueda));
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<Categoria>> listarCategorias() {
        return ResponseEntity.ok(productoService.listarCategorias());
    }

    @GetMapping("/marcas")
    public ResponseEntity<List<Marca>> listarMarcas() {
        return ResponseEntity.ok(productoService.listarMarcas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PostMapping("/save")
    public ResponseEntity<ProductoResponse> guardar(@RequestBody ProductoRequest request) {
        logger.info(">guardarProducto: {}", request.getNombre());
        return ResponseEntity.ok(productoService.guardarProducto(request));
    }
}