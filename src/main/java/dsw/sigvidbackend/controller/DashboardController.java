package dsw.sigvidbackend.controller;

import dsw.sigvidbackend.dto.PedidosEstadoResponse;
import dsw.sigvidbackend.dto.StockCategoriaResponse;
import dsw.sigvidbackend.dto.VentaDiariaResponse;
import dsw.sigvidbackend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// endpoints de datos para los 3 charts del dashboard (linea, radar y polar area)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/ventas-diarias")
    public ResponseEntity<List<VentaDiariaResponse>> ventasDiarias(@RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(dashboardService.ventasDiarias(dias));
    }

    @GetMapping("/stock-categoria")
    public ResponseEntity<List<StockCategoriaResponse>> stockPorCategoria() {
        return ResponseEntity.ok(dashboardService.stockPorCategoria());
    }

    @GetMapping("/pedidos-estado")
    public ResponseEntity<List<PedidosEstadoResponse>> pedidosPorEstado() {
        return ResponseEntity.ok(dashboardService.pedidosPorEstado());
    }
}


