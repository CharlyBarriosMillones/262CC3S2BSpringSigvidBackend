package dsw.sigvidbackend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import dsw.sigvidbackend.model.Usuario;

import jakarta.validation.Valid;

import dsw.sigvidbackend.dto.PedidoRequest;
import dsw.sigvidbackend.dto.PedidoResponse;
import dsw.sigvidbackend.service.PedidoService;
import dsw.sigvidbackend.util.ObjectResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);
    private final PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarTodos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String termino) {
        if (estado != null && !estado.isBlank()) {
            return ResponseEntity.ok(pedidoService.listarPorEstado(estado));
        }
        if (termino != null && !termino.isBlank()) {
            return ResponseEntity.ok(pedidoService.buscarPedidos(termino));
        }
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    // historial del cliente: se usa el email del token (no el del request) para evitar ver pedidos ajenos
    @GetMapping("/cliente")
    public ResponseEntity<List<PedidoResponse>> listarPorCliente(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.listarPorEmailCliente(usuario.getEmail()));
    }

    @PostMapping
    public ResponseEntity<ObjectResponse<PedidoResponse>> registrarPedido(@Valid @RequestBody PedidoRequest request) {
        logger.info(">registrarPedido cliente={}", request.getDocumentoCliente());
        PedidoResponse response = pedidoService.registrarPedido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ObjectResponse.<PedidoResponse>builder()
                        .success(true)
                        .message("Pedido " + response.getCodigoPedido() + " registrado exitosamente")
                        .object(response)
                        .build()
        );
    }

    @PutMapping("/{id}/derivar-almacen")
    public ResponseEntity<ObjectResponse<PedidoResponse>> derivarAAlmacen(@PathVariable Long id) {
        logger.info(">derivarAAlmacen idPedido={}", id);
        PedidoResponse response = pedidoService.derivarPedidoAAlmacen(id);
        return ResponseEntity.ok(
                ObjectResponse.<PedidoResponse>builder()
                        .success(true)
                        .message(response.getNotificacionAlmacen())
                        .object(response)
                        .build()
        );
    }
}