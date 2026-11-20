package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.AsignacionRequest;
import dsw.sigvidbackend.dto.GuiaDespachoResponse;
import dsw.sigvidbackend.dto.PedidoAlmacenDetalleResponse;
import dsw.sigvidbackend.dto.PedidoResponse;
import dsw.sigvidbackend.model.*;
import dsw.sigvidbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

// HU08 - asignar productos a pedido (guia preliminar) y HU09 - registrar salida de productos (QR)
@Service
@RequiredArgsConstructor
public class AlmacenService {

    static final String EN_PREPARACION = "En preparación";
    static final String EN_TRANSITO = "En tránsito";

    private final PedidoRepository pedidoRepository;
    private final LineaPedidoRepository lineaPedidoRepository;
    private final FacturaElectronicaRepository facturaElectronicaRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final AlmacenRepository almacenRepository;
    private final StockRepository stockRepository;
    private final GuiaDespachoRepository guiaDespachoRepository;
    private final DetalleGuiaRepository detalleGuiaRepository;

    // ---------------------------------------------------------------- HU08

    // pedidos derivados por el vendedor ("En preparación") que aun no tienen guia
    public List<PedidoResponse> listarPedidosPendientes() {
        return pedidoRepository.findByEstado_NombreIgnoreCaseOrderByFechaSolicitudDesc(EN_PREPARACION).stream()
                .filter(p -> !guiaDespachoRepository.existsByPedido_IdPedido(p.getIdPedido()))
                .map(p -> PedidoResponse.fromEntity(p,
                        lineaPedidoRepository.findByPedido_IdPedido(p.getIdPedido()),
                        facturaElectronicaRepository.findByPedido_IdPedido(p.getIdPedido()).orElse(null),
                        null))
                .toList();
    }

    // detalle del pedido: cantidades solicitadas vs stock disponible en el almacen elegido
    public PedidoAlmacenDetalleResponse obtenerDetalle(Long idPedido, Long idAlmacen) {
        Pedido pedido = obtenerPedidoPendiente(idPedido);
        Almacen almacen = obtenerAlmacen(idAlmacen);
        List<PedidoAlmacenDetalleResponse.Linea> lineas = new ArrayList<>();
        for (Map.Entry<Producto, Integer> e : cantidadesSolicitadas(idPedido).entrySet()) {
            int disponible = stockDisponible(e.getKey().getIdProducto(), almacen.getIdAlmacen());
            lineas.add(PedidoAlmacenDetalleResponse.Linea.builder()
                    .idProducto(e.getKey().getIdProducto())
                    .codigoProducto(e.getKey().getCodigoProducto())
                    .nombreProducto(e.getKey().getNombre())
                    .cantidadSolicitada(e.getValue())
                    .stockDisponible(disponible)
                    .cantidadSugerida(Math.min(e.getValue(), disponible))
                    .stockSuficiente(disponible >= e.getValue())
                    .build());
        }
        return PedidoAlmacenDetalleResponse.builder()
                .idPedido(pedido.getIdPedido())
                .codigoPedido(pedido.getCodigoPedido())
                .estado(pedido.getEstado().getNombre())
                .nombreCliente(pedido.getCliente().getNombre() + " " + pedido.getCliente().getApellido())
                .dniCliente(pedido.getCliente().getDni())
                .nombreSucursal(pedido.getSucursal().getNombre())
                .idAlmacen(almacen.getIdAlmacen())
                .nombreAlmacen(almacen.getNombre())
                .lineas(lineas)
                .build();
    }

    // confirma la asignacion (ajustando cantidades si no hay stock) y genera la guia de despacho preliminar
    @Transactional
    public GuiaDespachoResponse asignarProductos(Long idPedido, AsignacionRequest request, Usuario trabajador) {
        Pedido pedido = obtenerPedidoPendiente(idPedido);
        Almacen almacen = obtenerAlmacen(request.getIdAlmacen());
        Map<Producto, Integer> solicitadas = cantidadesSolicitadas(idPedido);

        Map<Long, Integer> asignadas = new HashMap<>();
        for (AsignacionRequest.Linea l : request.getLineas()) {
            if (asignadas.put(l.getIdProducto(), l.getCantidadAsignada()) != null) {
                throw new IllegalArgumentException("El producto con ID " + l.getIdProducto() + " está repetido en la asignación");
            }
        }

        List<DetalleGuia> detalles = new ArrayList<>();
        boolean parcial = false;
        for (Map.Entry<Producto, Integer> e : solicitadas.entrySet()) {
            Producto producto = e.getKey();
            Integer asignada = asignadas.remove(producto.getIdProducto());
            if (asignada == null) {
                throw new IllegalArgumentException("Falta indicar la cantidad asignada del producto " + producto.getCodigoProducto());
            }
            if (asignada > e.getValue()) {
                throw new IllegalArgumentException("La cantidad asignada de " + producto.getCodigoProducto()
                        + " (" + asignada + ") supera lo solicitado (" + e.getValue() + ")");
            }
            int disponible = stockDisponible(producto.getIdProducto(), almacen.getIdAlmacen());
            if (asignada > disponible) {
                throw new IllegalArgumentException("Stock insuficiente de " + producto.getCodigoProducto() + " en "
                        + almacen.getNombre() + ": disponible " + disponible + ", asignado " + asignada);
            }
            if (asignada < e.getValue()) {
                parcial = true;
            }
            if (asignada > 0) {
                detalles.add(DetalleGuia.builder()
                        .producto(producto)
                        .cantidadSolicitada(e.getValue())
                        .cantidadAsignada(asignada)
                        .escaneado(false)
                        .build());
            }
        }
        if (!asignadas.isEmpty()) {
            throw new IllegalArgumentException("Hay productos en la asignación que no pertenecen al pedido: " + asignadas.keySet());
        }
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("Debe asignar al menos una unidad de algún producto");
        }

        GuiaDespacho guia = GuiaDespacho.builder()
                .codigoGuia(generarCodigoGuia())
                .pedido(pedido)
                .almacen(almacen)
                .trabajador(trabajador)
                .fechaGeneracion(LocalDateTime.now())
                .estado(GuiaDespacho.PRELIMINAR)
                .build();
        guia = guiaDespachoRepository.saveAndFlush(guia);

        for (DetalleGuia d : detalles) {
            d.setGuia(guia);
            d.setCodigoQr(generarCodigoQr(guia, d));
        }
        detalles = detalleGuiaRepository.saveAllAndFlush(detalles);

        String notificacion = "Asignación confirmada. Pedido " + pedido.getCodigoPedido() + " en preparación con guía de despacho preliminar "
                + guia.getCodigoGuia() + (parcial ? " (asignación parcial por stock insuficiente)." : ".");
        return GuiaDespachoResponse.fromEntity(guia, detalles, notificacion);
    }

    // ---------------------------------------------------------------- HU09

    // guias por estado (Preliminar = listas para despachar)
    public List<GuiaDespachoResponse> listarGuias(String estado) {
        String est = (estado == null || estado.isBlank()) ? GuiaDespacho.PRELIMINAR : estado.trim();
        return guiaDespachoRepository.findByEstadoOrderByFechaGeneracionAsc(est).stream()
                .map(g -> GuiaDespachoResponse.fromEntity(g, detalleGuiaRepository.findByGuia_IdGuiaOrderByIdDetalleAsc(g.getIdGuia()), null))
                .toList();
    }

    public GuiaDespachoResponse obtenerGuia(Long idGuia) {
        GuiaDespacho guia = buscarGuia(idGuia);
        return GuiaDespachoResponse.fromEntity(guia, detalleGuiaRepository.findByGuia_IdGuiaOrderByIdDetalleAsc(idGuia), null);
    }

    // registra el escaneo del QR de un item (progreso escaneados vs total)
    @Transactional
    public GuiaDespachoResponse registrarEscaneo(Long idGuia, Long idDetalle, String codigoQr) {
        GuiaDespacho guia = buscarGuiaPreliminar(idGuia);
        DetalleGuia detalle = detalleGuiaRepository.findById(idDetalle)
                .filter(d -> d.getGuia().getIdGuia().equals(idGuia))
                .orElseThrow(() -> new NoSuchElementException("El item " + idDetalle + " no pertenece a la guía " + guia.getCodigoGuia()));
        // el codigo leido del QR mostrado en pantalla es obligatorio y debe coincidir con el del item
        if (codigoQr == null || codigoQr.isBlank()) {
            throw new IllegalArgumentException("Debe enviar el código QR leído del producto " + detalle.getProducto().getCodigoProducto());
        }
        if (!codigoQr.trim().equals(detalle.getCodigoQr())) {
            throw new IllegalArgumentException("El código QR leído no corresponde al producto " + detalle.getProducto().getCodigoProducto());
        }
        detalle.setEscaneado(true);
        detalleGuiaRepository.saveAndFlush(detalle);
        List<DetalleGuia> detalles = detalleGuiaRepository.findByGuia_IdGuiaOrderByIdDetalleAsc(idGuia);
        return GuiaDespachoResponse.fromEntity(guia, detalles, "Producto " + detalle.getProducto().getCodigoProducto() + " escaneado.");
    }

    // confirma la salida: descuenta stock, guia "Despachada" y pedido "En tránsito"
    @Transactional
    public GuiaDespachoResponse confirmarSalida(Long idGuia) {
        GuiaDespacho guia = buscarGuiaPreliminar(idGuia);
        List<DetalleGuia> detalles = detalleGuiaRepository.findByGuia_IdGuiaOrderByIdDetalleAsc(idGuia);
        long pendientes = detalles.stream().filter(d -> !Boolean.TRUE.equals(d.getEscaneado())).count();
        if (pendientes > 0) {
            throw new IllegalArgumentException("Faltan escanear " + pendientes + " de " + detalles.size() + " productos de la guía " + guia.getCodigoGuia());
        }
        for (DetalleGuia d : detalles) {
            Stock stock = stockRepository.findByProducto_IdProductoAndAlmacen_IdAlmacen(
                            d.getProducto().getIdProducto(), guia.getAlmacen().getIdAlmacen())
                    .orElseThrow(() -> new IllegalArgumentException("No hay stock registrado de " + d.getProducto().getCodigoProducto()
                            + " en " + guia.getAlmacen().getNombre()));
            if (stock.getCantidadActual() < d.getCantidadAsignada()) {
                throw new IllegalArgumentException("Stock insuficiente de " + d.getProducto().getCodigoProducto() + " al dar salida: disponible "
                        + stock.getCantidadActual() + ", requerido " + d.getCantidadAsignada());
            }
            stock.setCantidadActual(stock.getCantidadActual() - d.getCantidadAsignada());
            stockRepository.save(stock);
        }
        stockRepository.flush();

        guia.setEstado(GuiaDespacho.DESPACHADA);
        guia.setFechaSalida(LocalDateTime.now());
        guiaDespachoRepository.saveAndFlush(guia);

        Pedido pedido = guia.getPedido();
        pedido.setEstado(buscarEstado(EN_TRANSITO, 4L));
        pedidoRepository.saveAndFlush(pedido);

        String notificacion = "Salida confirmada. Stock actualizado y pedido " + pedido.getCodigoPedido()
                + " en tránsito. Se notificó al cliente " + pedido.getCliente().getNombre() + " para confirmar la recepción.";
        return GuiaDespachoResponse.fromEntity(guia, detalles, notificacion);
    }

    // ---------------------------------------------------------------- auxiliares

    private Pedido obtenerPedidoPendiente(Long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado con ID: " + idPedido));
        if (!EN_PREPARACION.equalsIgnoreCase(pedido.getEstado().getNombre())) {
            throw new IllegalArgumentException("Solo se asignan productos a pedidos derivados a almacén ('En preparación'); el pedido "
                    + pedido.getCodigoPedido() + " está en '" + pedido.getEstado().getNombre() + "'");
        }
        if (guiaDespachoRepository.existsByPedido_IdPedido(idPedido)) {
            throw new IllegalArgumentException("El pedido " + pedido.getCodigoPedido() + " ya tiene una guía de despacho generada");
        }
        return pedido;
    }

    private Almacen obtenerAlmacen(Long idAlmacen) {
        if (idAlmacen != null) {
            return almacenRepository.findById(idAlmacen)
                    .orElseThrow(() -> new NoSuchElementException("Almacén no encontrado con ID: " + idAlmacen));
        }
        return almacenRepository.findByActivoTrueOrderByNombreAsc().stream().findFirst()
                .orElseThrow(() -> new NoSuchElementException("No hay almacenes registrados"));
    }

    // suma las cantidades por producto (un producto podria repetirse en varias lineas)
    private Map<Producto, Integer> cantidadesSolicitadas(Long idPedido) {
        Map<Long, Producto> productos = new LinkedHashMap<>();
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (LineaPedido l : lineaPedidoRepository.findByPedido_IdPedido(idPedido)) {
            productos.putIfAbsent(l.getProducto().getIdProducto(), l.getProducto());
            cantidades.merge(l.getProducto().getIdProducto(), l.getCantidad(), Integer::sum);
        }
        Map<Producto, Integer> resultado = new LinkedHashMap<>();
        productos.forEach((id, p) -> resultado.put(p, cantidades.get(id)));
        return resultado;
    }

    // disponible = stock actual - lo ya reservado en guias "Preliminar" (asignadas y aun no despachadas)
    private int stockDisponible(Long idProducto, Long idAlmacen) {
        int actual = stockRepository.findByProducto_IdProductoAndAlmacen_IdAlmacen(idProducto, idAlmacen)
                .map(Stock::getCantidadActual)
                .orElse(0);
        int reservado = detalleGuiaRepository
                .findByGuia_EstadoAndGuia_Almacen_IdAlmacenAndProducto_IdProducto(GuiaDespacho.PRELIMINAR, idAlmacen, idProducto)
                .stream().mapToInt(DetalleGuia::getCantidadAsignada).sum();
        return Math.max(0, actual - reservado);
    }

    private GuiaDespacho buscarGuia(Long idGuia) {
        return guiaDespachoRepository.findById(idGuia)
                .orElseThrow(() -> new NoSuchElementException("Guía de despacho no encontrada con ID: " + idGuia));
    }

    private GuiaDespacho buscarGuiaPreliminar(Long idGuia) {
        GuiaDespacho guia = buscarGuia(idGuia);
        if (!GuiaDespacho.PRELIMINAR.equals(guia.getEstado())) {
            throw new IllegalArgumentException("La guía " + guia.getCodigoGuia() + " ya fue despachada");
        }
        return guia;
    }

    private EstadoPedido buscarEstado(String nombre, Long idRespaldo) {
        return estadoPedidoRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> estadoPedidoRepository.findById(idRespaldo)
                        .orElseThrow(() -> new NoSuchElementException("Estado '" + nombre + "' no encontrado en la base de datos")));
    }

    // codigo correlativo GD-000001
    private String generarCodigoGuia() {
        long correlativo = guiaDespachoRepository.count() + 1;
        String codigo = String.format("GD-%06d", correlativo);
        while (guiaDespachoRepository.existsByCodigoGuia(codigo)) {
            correlativo++;
            codigo = String.format("GD-%06d", correlativo);
        }
        return codigo;
    }

    // contenido del QR: sistema | guia | pedido | producto | cantidad
    static String generarCodigoQr(GuiaDespacho guia, DetalleGuia d) {
        return "SIGVID|" + guia.getCodigoGuia() + "|" + guia.getPedido().getCodigoPedido() + "|"
                + d.getProducto().getCodigoProducto() + "|" + d.getCantidadAsignada();
    }
}
