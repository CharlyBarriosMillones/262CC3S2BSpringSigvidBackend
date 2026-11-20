package dsw.sigvidbackend.controller;

import dsw.sigvidbackend.dto.AlmacenResponse;
import dsw.sigvidbackend.dto.StockResponse;
import dsw.sigvidbackend.model.Ubigeo;
import dsw.sigvidbackend.model.Usuario;
import dsw.sigvidbackend.service.StockService;
import dsw.sigvidbackend.util.ObjectResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// HU07 - consultar stock (trabajador de almacen)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StockController {

    private static final Logger logger = LoggerFactory.getLogger(StockController.class);
    private final StockService stockService;

    @GetMapping("/stock")
    public ResponseEntity<List<StockResponse>> consultarStock(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) Long idCategoria,
            @RequestParam(required = false) Long idAlmacen,
            @RequestParam(required = false) String idUbigeo,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(stockService.consultarStock(termino, idCategoria, idAlmacen, idUbigeo, estado));
    }

    @PutMapping("/stock/{id}/confirmar")
    public ResponseEntity<ObjectResponse<StockResponse>> confirmarStock(
            @PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        logger.info(">confirmarStock idStock={}", id);
        StockResponse r = stockService.confirmarStock(id, usuario);
        return ResponseEntity.ok(ObjectResponse.<StockResponse>builder()
                .success(true).message(r.getMensaje()).object(r).build());
    }

    @GetMapping("/almacenes")
    public ResponseEntity<List<AlmacenResponse>> listarAlmacenes() {
        return ResponseEntity.ok(stockService.listarAlmacenes());
    }

    @GetMapping("/ubigeos")
    public ResponseEntity<List<Ubigeo>> listarUbigeos() {
        return ResponseEntity.ok(stockService.listarUbigeos());
    }
}

