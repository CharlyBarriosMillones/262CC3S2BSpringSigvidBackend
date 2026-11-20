package dsw.sigvidbackend.controller;

import dsw.sigvidbackend.dto.AsignacionRequest;
import dsw.sigvidbackend.dto.GuiaDespachoResponse;
import dsw.sigvidbackend.dto.PedidoAlmacenDetalleResponse;
import dsw.sigvidbackend.dto.PedidoResponse;
import dsw.sigvidbackend.model.Usuario;
import dsw.sigvidbackend.service.AlmacenService;
import dsw.sigvidbackend.util.ObjectResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// HU08 - asignar productos a pedido y HU09 - registrar salida de productos (QR)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/almacen")
public class AlmacenController {

    private static final Logger logger = LoggerFactory.getLogger(AlmacenController.class);
    private final AlmacenService almacenService;

    // HU08: pedidos derivados ("En preparación") pendientes de asignacion
    @GetMapping("/pedidos-pendientes")
    public ResponseEntity<List<PedidoResponse>> listarPedidosPendientes() {
        return ResponseEntity.ok(almacenService.listarPedidosPendientes());
    }

    // HU08: detalle con cantidades solicitadas y stock disponible
    @GetMapping("/pedidos/{id}/detalle")
    public ResponseEntity<PedidoAlmacenDetalleResponse> obtenerDetalle(
            @PathVariable Long id, @RequestParam(required = false) Long idAlmacen) {
        return ResponseEntity.ok(almacenService.obtenerDetalle(id, idAlmacen));
    }

    // HU08: confirmar asignacion y generar guia de despacho preliminar
    @PostMapping("/pedidos/{id}/asignar")
    public ResponseEntity<ObjectResponse<GuiaDespachoResponse>> asignar(
            @PathVariable Long id,
            @Valid @RequestBody AsignacionRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        logger.info(">asignarProductos idPedido={}", id);
        GuiaDespachoResponse r = almacenService.asignarProductos(id, request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(ObjectResponse.<GuiaDespachoResponse>builder()
                .success(true).message(r.getNotificacion()).object(r).build());
    }

    // HU09: guias por estado (por defecto "Preliminar" = listas para despachar)
    @GetMapping("/guias")
    public ResponseEntity<List<GuiaDespachoResponse>> listarGuias(@RequestParam(required = false) String estado) {
        return ResponseEntity.ok(almacenService.listarGuias(estado));
    }

    @GetMapping("/guias/{id}")
    public ResponseEntity<GuiaDespachoResponse> obtenerGuia(@PathVariable Long id) {
        return ResponseEntity.ok(almacenService.obtenerGuia(id));
    }

    // HU09: registrar el escaneo del QR de un item (opcionalmente con el contenido leido)
    @PutMapping("/guias/{id}/detalles/{idDetalle}/escanear")
    public ResponseEntity<ObjectResponse<GuiaDespachoResponse>> escanear(
            @PathVariable Long id,
            @PathVariable Long idDetalle,
            @RequestBody(required = false) Map<String, String> body) {
        String codigoQr = body != null ? body.get("codigoQr") : null;
        GuiaDespachoResponse r = almacenService.registrarEscaneo(id, idDetalle, codigoQr);
        return ResponseEntity.ok(ObjectResponse.<GuiaDespachoResponse>builder()
                .success(true).message(r.getNotificacion()).object(r).build());
    }

    // HU09: confirmar salida -> descuenta stock y pedido "En tránsito"
    @PostMapping("/guias/{id}/confirmar-salida")
    public ResponseEntity<ObjectResponse<GuiaDespachoResponse>> confirmarSalida(@PathVariable Long id) {
        logger.info(">confirmarSalida idGuia={}", id);
        GuiaDespachoResponse r = almacenService.confirmarSalida(id);
        return ResponseEntity.ok(ObjectResponse.<GuiaDespachoResponse>builder()
                .success(true).message(r.getNotificacion()).object(r).build());
    }
}
