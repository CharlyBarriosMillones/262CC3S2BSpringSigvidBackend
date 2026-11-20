package dsw.sigvidbackend.controller;

import dsw.sigvidbackend.dto.PedidosEstadoResponse;
import dsw.sigvidbackend.dto.StockCategoriaResponse;
import dsw.sigvidbackend.dto.VentaDiariaResponse;
import dsw.sigvidbackend.model.Usuario;
import dsw.sigvidbackend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// endpoints de datos para los 3 charts del dashboard (linea, radar y polar area)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/ventas-diarias")
    public ResponseEntity<List<VentaDiariaResponse>> ventasDiarias(
            @RequestParam(defaultValue = "30") int dias,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(dashboardService.ventasDiarias(dias, usuario));
    }

    @GetMapping("/stock-categoria")
    public ResponseEntity<List<StockCategoriaResponse>> stockPorCategoria(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(dashboardService.stockPorCategoria(usuario));
    }

    @GetMapping("/pedidos-estado")
    public ResponseEntity<List<PedidosEstadoResponse>> pedidosPorEstado(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(dashboardService.pedidosPorEstado(usuario));
    }
}