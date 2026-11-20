package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.PedidosEstadoResponse;
import dsw.sigvidbackend.dto.StockCategoriaResponse;
import dsw.sigvidbackend.dto.VentaDiariaResponse;
import dsw.sigvidbackend.model.EstadoPedido;
import dsw.sigvidbackend.model.Pedido;
import dsw.sigvidbackend.model.Stock;
import dsw.sigvidbackend.repository.CategoriaRepository;
import dsw.sigvidbackend.repository.EstadoPedidoRepository;
import dsw.sigvidbackend.repository.PedidoRepository;
import dsw.sigvidbackend.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

// datos agregados para los charts del dashboard (endpoint GET de agregacion por chart, como NPersonasXTipoDocumento
// del proyecto base; aqui la agregacion se hace en memoria porque el volumen de datos del curso es pequeño)
// stock-categoria: el minimo se suma por cada fila producto x almacen (minimo exigido en cada almacen)
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PedidoRepository pedidoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final StockRepository stockRepository;
    private final CategoriaRepository categoriaRepository;

    // chart de lineas: monto cobrado por dia (pedidos con fecha de pago) en los ultimos N dias, incluye dias en cero
    public List<VentaDiariaResponse> ventasDiarias(int dias) {
        int rango = Math.max(1, Math.min(dias, 365));
        LocalDate hoy = LocalDate.now();
        LocalDate desde = hoy.minusDays(rango - 1L);
        Map<LocalDate, List<Pedido>> porDia = pedidoRepository.findAll().stream()
                .filter(p -> p.getFechaPago() != null)
                .filter(p -> !p.getFechaPago().toLocalDate().isBefore(desde) && !p.getFechaPago().toLocalDate().isAfter(hoy))
                .collect(Collectors.groupingBy(p -> p.getFechaPago().toLocalDate()));
        List<VentaDiariaResponse> serie = new ArrayList<>();
        for (LocalDate d = desde; !d.isAfter(hoy); d = d.plusDays(1)) {
            List<Pedido> lista = porDia.getOrDefault(d, List.of());
            BigDecimal total = lista.stream().map(Pedido::getImporteTotal).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            serie.add(new VentaDiariaResponse(d, total, (long) lista.size()));
        }
        return serie;
    }

    // chart radar: suma de stock actual y de stock minimo por categoria (todas las categorias)
    public List<StockCategoriaResponse> stockPorCategoria() {
        Map<String, long[]> acumulado = new LinkedHashMap<>();
        categoriaRepository.findAll().forEach(c -> acumulado.put(c.getNombre(), new long[2]));
        for (Stock s : stockRepository.findAll()) {
            if (s.getProducto().getCategoria() == null) continue;
            long[] v = acumulado.computeIfAbsent(s.getProducto().getCategoria().getNombre(), k -> new long[2]);
            v[0] += s.getCantidadActual() != null ? s.getCantidadActual() : 0;
            v[1] += s.getProducto().getStockMinimo() != null ? s.getProducto().getStockMinimo() : 0;
        }
        return acumulado.entrySet().stream()
                .map(e -> new StockCategoriaResponse(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .toList();
    }

    // chart polar area: cantidad de pedidos por estado (todos los estados, aunque tengan cero)
    public List<PedidosEstadoResponse> pedidosPorEstado() {
        Map<String, Long> conteo = pedidoRepository.findAll().stream()
                .collect(Collectors.groupingBy(p -> p.getEstado().getNombre(), Collectors.counting()));
        List<PedidosEstadoResponse> resultado = new ArrayList<>();
        estadoPedidoRepository.findAll().stream()
                .sorted(Comparator.comparing(EstadoPedido::getIdEstado))
                .forEach(e -> resultado.add(new PedidosEstadoResponse(e.getNombre(), conteo.getOrDefault(e.getNombre(), 0L))));
        return resultado;
    }
}
