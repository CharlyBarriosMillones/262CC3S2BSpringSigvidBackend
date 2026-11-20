package dsw.sigvidbackend.controller;

import dsw.sigvidbackend.dto.GuiaDespachoResponse;
import dsw.sigvidbackend.dto.RecepcionRequest;
import dsw.sigvidbackend.model.Usuario;
import dsw.sigvidbackend.service.RecepcionService;
import dsw.sigvidbackend.util.ObjectResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// HU10 - confirmar recepcion de pedido (cliente autenticado)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recepcion")
public class RecepcionController {

    private static final Logger logger = LoggerFactory.getLogger(RecepcionController.class);
    private final RecepcionService recepcionService;

    // pedidos del cliente en transito (el cliente sale del token)
    @GetMapping("/pedidos")
    public ResponseEntity<List<GuiaDespachoResponse>> listarEnTransito(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(recepcionService.listarPedidosEnTransito(usuario));
    }

    @PostMapping("/pedidos/{id}/confirmar")
    public ResponseEntity<ObjectResponse<GuiaDespachoResponse>> confirmar(
            @PathVariable Long id,
            @Valid @RequestBody RecepcionRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        logger.info(">confirmarRecepcion idPedido={} tipo={}", id, request.getTipo());
        GuiaDespachoResponse r = recepcionService.confirmarRecepcion(id, request, usuario);
        return ResponseEntity.ok(ObjectResponse.<GuiaDespachoResponse>builder()
                .success(true).message(r.getNotificacion()).object(r).build());
    }
}
