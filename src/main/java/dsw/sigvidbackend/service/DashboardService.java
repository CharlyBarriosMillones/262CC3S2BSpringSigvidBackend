package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.PedidosEstadoResponse;
import dsw.sigvidbackend.dto.StockCategoriaResponse;
import dsw.sigvidbackend.dto.VentaDiariaResponse;
import dsw.sigvidbackend.model.*;
import dsw.sigvidbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PedidoRepository pedidoRepository;
    private final LineaPedidoRepository lineaPedidoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final StockRepository stockRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    private boolean esCliente(Usuario usuario) {
        return usuario != null
                && usuario.getRol() != null
                && "Cliente".equalsIgnoreCase(usuario.getRol().getNombre());
    }

    // 1. Line Chart: personal ve ventas cobradas de la empresa, cliente ve solo sus compras
    public List<VentaDiariaResponse> ventasDiarias(int dias, Usuario usuario) {
        int rango = Math.max(1, Math.min(dias, 365));
        LocalDate hoy = LocalDate.now();
        LocalDate desde = hoy.minusDays(rango - 1L);

        boolean soloCliente = esCliente(usuario);
        List<Pedido> fuente = soloCliente
                ? pedidoRepository.findByCliente_EmailOrderByFechaSolicitudDesc(usuario.getEmail())
                : pedidoRepository.findAll();

        Map<LocalDate, List<Pedido>> porDia = fuente.stream()
                .filter(p -> soloCliente ? p.getFechaSolicitud() != null : p.getFechaPago() != null)
                .collect(Collectors.groupingBy(p -> soloCliente
                        ? (p.getFechaPago() != null ? p.getFechaPago().toLocalDate() : p.getFechaSolicitud().toLocalDate())
                        : p.getFechaPago().toLocalDate()));

        List<VentaDiariaResponse> serie = new ArrayList<>();
        for (LocalDate d = desde; !d.isAfter(hoy); d = d.plusDays(1)) {
            List<Pedido> lista = porDia.getOrDefault(d, List.of());
            BigDecimal total = lista.stream()
                    .map(Pedido::getImporteTotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            serie.add(new VentaDiariaResponse(d, total, (long) lista.size()));
        }
        return serie;
    }

    // 2. Radar Chart: personal ve stock actual vs minimo, cliente ve sus unidades compradas vs variedad del catalogo
    public List<StockCategoriaResponse> stockPorCategoria(Usuario usuario) {
        Map<String, long[]> acumulado = new LinkedHashMap<>();
        categoriaRepository.findAll().forEach(c -> acumulado.put(c.getNombre(), new long[2]));

        if (esCliente(usuario)) {
            List<Pedido> misPedidos = pedidoRepository.findByCliente_EmailOrderByFechaSolicitudDesc(usuario.getEmail());
            for (Pedido p : misPedidos) {
                for (LineaPedido linea : lineaPedidoRepository.findByPedido_IdPedido(p.getIdPedido())) {
                    if (linea.getProducto() == null || linea.getProducto().getCategoria() == null) continue;
                    String cat = linea.getProducto().getCategoria().getNombre();
                    long[] v = acumulado.computeIfAbsent(cat, k -> new long[2]);
                    v[0] += linea.getCantidad() != null ? linea.getCantidad() : 0;
                }
            }
            for (Producto prod : productoRepository.findAllByOrderByIdProductoAsc()) {
                if (Boolean.FALSE.equals(prod.getActivo()) || prod.getCategoria() == null) continue;
                String cat = prod.getCategoria().getNombre();
                long[] v = acumulado.computeIfAbsent(cat, k -> new long[2]);
                v[1] += 1;
            }
        } else {
            for (Stock s : stockRepository.findAll()) {
                if (s.getProducto().getCategoria() == null) continue;
                long[] v = acumulado.computeIfAbsent(s.getProducto().getCategoria().getNombre(), k -> new long[2]);
                v[0] += s.getCantidadActual() != null ? s.getCantidadActual() : 0;
                v[1] += s.getProducto().getStockMinimo() != null ? s.getProducto().getStockMinimo() : 0;
            }
        }

        return acumulado.entrySet().stream()
                .map(e -> new StockCategoriaResponse(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .toList();
    }

    // 3. Polar Area Chart: personal ve todos los pedidos; cliente ve solo el estado de sus pedidos
    public List<PedidosEstadoResponse> pedidosPorEstado(Usuario usuario) {
        List<Pedido> fuente = esCliente(usuario)
                ? pedidoRepository.findByCliente_EmailOrderByFechaSolicitudDesc(usuario.getEmail())
                : pedidoRepository.findAll();

        Map<String, Long> conteo = fuente.stream()
                .collect(Collectors.groupingBy(p -> p.getEstado().getNombre(), Collectors.counting()));

        List<PedidosEstadoResponse> resultado = new ArrayList<>();
        estadoPedidoRepository.findAll().stream()
                .sorted(Comparator.comparing(EstadoPedido::getIdEstado))
                .forEach(e -> resultado.add(new PedidosEstadoResponse(e.getNombre(), conteo.getOrDefault(e.getNombre(), 0L))));
        return resultado;
    }
}