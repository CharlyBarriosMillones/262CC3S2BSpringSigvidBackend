package dsw.sigvidbackend.controller;

import jakarta.validation.Valid;

import dsw.sigvidbackend.dto.PagoRequest;
import dsw.sigvidbackend.dto.PagoResponse;
import dsw.sigvidbackend.model.Caja;
import dsw.sigvidbackend.model.MetodoPago;
import dsw.sigvidbackend.service.PagoService;
import dsw.sigvidbackend.util.ObjectResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private static final Logger logger = LoggerFactory.getLogger(PagoController.class);
    private final PagoService pagoService;

    @GetMapping
    public ResponseEntity<List<PagoResponse>> listarPagos() {
        return ResponseEntity.ok(pagoService.listarPagos());
    }

    @GetMapping("/metodos")
    public ResponseEntity<List<MetodoPago>> listarMetodos() {
        return ResponseEntity.ok(pagoService.listarMetodosPago());
    }

    @GetMapping("/cajas")
    public ResponseEntity<List<Caja>> listarCajas() {
        return ResponseEntity.ok(pagoService.listarCajas());
    }

    @PostMapping
    public ResponseEntity<ObjectResponse<PagoResponse>> registrarPago(@Valid @RequestBody PagoRequest request) {
        logger.info(">registrarPago idPedido={}", request.getIdPedido());
        PagoResponse response = pagoService.registrarPago(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ObjectResponse.<PagoResponse>builder()
                        .success(true)
                        .message("Pago registrado exitosamente. Pedido actualizado a 'Pagado'.")
                        .object(response)
                        .build()
        );
    }
}