package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.LineaPedidoRequest;
import dsw.sigvidbackend.dto.PedidoRequest;
import dsw.sigvidbackend.dto.PedidoResponse;
import dsw.sigvidbackend.model.*;
import dsw.sigvidbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final LineaPedidoRepository lineaPedidoRepository;
    private final ProductoRepository productoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final VendedorRepository vendedorRepository;
    private final FacturaElectronicaRepository facturaElectronicaRepository;

    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaSolicitudDesc().stream()
                .map(this::construirRespuesta)
                .collect(Collectors.toList());
    }

    public List<PedidoResponse> listarPorEstado(String estado) {
        return pedidoRepository.findByEstado_NombreIgnoreCaseOrderByFechaSolicitudDesc(estado.trim()).stream()
                .map(this::construirRespuesta)
                .collect(Collectors.toList());
    }

    public List<PedidoResponse> listarPorEmailCliente(String email) {
        return pedidoRepository.findByCliente_EmailOrderByFechaSolicitudDesc(email.trim()).stream()
                .map(this::construirRespuesta)
                .collect(Collectors.toList());
    }

    public List<PedidoResponse> buscarPedidos(String termino) {
        if (termino == null || termino.isBlank()) {
            return listarTodos();
        }
        return pedidoRepository.buscarPorCodigoODniCliente(termino.trim()).stream()
                .map(this::construirRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public PedidoResponse registrarPedido(PedidoRequest request) {
        if (request.getLineas() == null || request.getLineas().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe incluir al menos un producto");
        }

        // obtener o buscar el cliente por ID, DNI (8 digitos) o RUC (11 digitos)
        Usuario cliente;
        if (request.getIdCliente() != null) {
            cliente = usuarioRepository.findById(request.getIdCliente())
                    .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado con ID: " + request.getIdCliente()));
        } else if (request.getDocumentoCliente() != null && !request.getDocumentoCliente().isBlank()) {
            String doc = request.getDocumentoCliente().trim();
            if (doc.length() == 11) {
                Long idUsuarioRuc = clienteRepository.findByRuc(doc)
                        .orElseThrow(() -> new NoSuchElementException("No se encontró cliente con RUC: " + doc))
                        .getIdUsuario();
                cliente = usuarioRepository.findById(idUsuarioRuc)
                        .orElseThrow(() -> new NoSuchElementException("No se encontró el usuario del RUC: " + doc));
            } else {
                cliente = usuarioRepository.findByDni(doc)
                        .orElseThrow(() -> new NoSuchElementException("No se encontró cliente con DNI: " + doc));
            }
        } else {
            throw new IllegalArgumentException("Debe indicar el ID, DNI o RUC del cliente");
        }

        String rolCliente = cliente.getRol() != null ? cliente.getRol().getNombre() : "";
        if (!"Cliente".equalsIgnoreCase(rolCliente)) {
            throw new IllegalArgumentException("El documento ingresado no corresponde a un cliente registrado.");
        }
        
        // asegurar registro en tabla "clientes" 
        if (!clienteRepository.existsById(cliente.getIdUsuario())) {
            clienteRepository.saveAndFlush(new Cliente(
                    cliente.getIdUsuario(),
                    (request.getClienteRuc() != null && request.getClienteRuc().length() == 11) ? request.getClienteRuc() : null,
                    null,
                    null
            ));
        }

        // obtener el vendedor (o usar usuario autenticado/por defecto)
        Usuario vendedor;
        if (request.getIdVendedor() != null) {
            vendedor = usuarioRepository.findById(request.getIdVendedor())
                    .orElse(cliente);
        } else {
            vendedor = cliente;
        }

        // asegurar registro en tabla vendedores
        if (!vendedorRepository.existsById(vendedor.getIdUsuario())) {
            vendedorRepository.saveAndFlush(new Vendedor(vendedor.getIdUsuario(), "Ventas Generales"));
        }

        // sucursal y estado inicial ("propuesto")
        Long idSucursal = request.getIdSucursal() != null ? request.getIdSucursal() : 1L;
        Sucursal sucursal = sucursalRepository.findById(idSucursal)
                .orElseThrow(() -> new NoSuchElementException("Sucursal no encontrada con ID: " + idSucursal));

        EstadoPedido estadoPropuesto = estadoPedidoRepository.findByNombreIgnoreCase("Propuesto")
                .orElseGet(() -> estadoPedidoRepository.findById(1L)
                        .orElseThrow(() -> new NoSuchElementException("Estado 'Propuesto' no encontrado en la base de datos")));

        // generar codigo unico de pedido (ej. PED-2026-0001)
        long correlativo = pedidoRepository.count() + 1;
        String codigoPedido = String.format("PED-2026-%04d", correlativo);
        while (pedidoRepository.findByCodigoPedidoIgnoreCase(codigoPedido).isPresent()) {
            correlativo++;
            codigoPedido = String.format("PED-2026-%04d", correlativo);
        }

        Pedido pedido = Pedido.builder()
                .codigoPedido(codigoPedido)
                .fechaSolicitud(LocalDateTime.now())
                .importeTotal(BigDecimal.ZERO)
                .descuento(BigDecimal.ZERO)
                .estado(estadoPropuesto)
                .sucursal(sucursal)
                .cliente(cliente)
                .vendedor(vendedor)
                .build();

        pedido = pedidoRepository.saveAndFlush(pedido);

        // procesar lineas del pedido y calcular subtotal bruto
        BigDecimal subtotalBruto = BigDecimal.ZERO;
        List<LineaPedido> lineasGuardadas = new ArrayList<>();

        for (LineaPedidoRequest item : request.getLineas()) {
            Producto producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new NoSuchElementException("Producto no encontrado con ID: " + item.getIdProducto()));

            int cantidad = (item.getCantidad() != null && item.getCantidad() > 0) ? item.getCantidad() : 1;
            BigDecimal precioUnit = item.getPrecioUnitario() != null ? item.getPrecioUnitario() : producto.getPrecioReferencial();
            BigDecimal subtotalLinea = precioUnit.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);

            LineaPedido linea = LineaPedido.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(cantidad)
                    .precioUnitario(precioUnit)
                    .descuentoLinea(BigDecimal.ZERO)
                    .subtotal(subtotalLinea)
                    .build();

            lineasGuardadas.add(lineaPedidoRepository.save(linea));
            subtotalBruto = subtotalBruto.add(subtotalLinea);
        }

        // aplicar descuento (porcentaje o monto fijo)
        BigDecimal valorDesc = request.getValorDescuento() != null ? request.getValorDescuento() : BigDecimal.ZERO;
        BigDecimal montoDescuento = BigDecimal.ZERO;
        BigDecimal porcentajeRegistrado = BigDecimal.ZERO;

        if (valorDesc.compareTo(BigDecimal.ZERO) > 0 && subtotalBruto.compareTo(BigDecimal.ZERO) > 0) {
            if ("MONTO_FIJO".equalsIgnoreCase(request.getTipoDescuento())) {
                montoDescuento = valorDesc.min(subtotalBruto);
                porcentajeRegistrado = montoDescuento.multiply(BigDecimal.valueOf(100))
                        .divide(subtotalBruto, 2, RoundingMode.HALF_UP);
            } else {
                porcentajeRegistrado = valorDesc.min(BigDecimal.valueOf(100));
                montoDescuento = subtotalBruto.multiply(porcentajeRegistrado)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal importeFinal = subtotalBruto.subtract(montoDescuento).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        pedido.setDescuento(porcentajeRegistrado);
        pedido.setImporteTotal(importeFinal);
        pedido = pedidoRepository.saveAndFlush(pedido);

        // generacion de factura electronica (simulando conexion a SUNAT)
        FacturaElectronica factura = null;
        boolean emitirFactura = request.getEmitirFacturaElectronica() == null || request.getEmitirFacturaElectronica();
        if (emitirFactura) {
            String rucFactura = (request.getClienteRuc() != null && request.getClienteRuc().trim().length() == 11)
                    ? request.getClienteRuc().trim()
                    : "10" + (cliente.getDni() != null ? cliente.getDni() : "00000000") + "9";

            String codigoFactura = String.format("F001-%06d", pedido.getIdPedido());
            factura = FacturaElectronica.builder()
                    .codigoFactura(codigoFactura)
                    .fechaEmision(LocalDateTime.now())
                    .montoTotal(importeFinal)
                    .clienteRuc(rucFactura)
                    .sunatEstado("Aceptada por SUNAT")
                    .pedido(pedido)
                    .build();
            factura = facturaElectronicaRepository.saveAndFlush(factura);
        }

        String notificacion = "Notificación enviada al Almacén (" + sucursal.getNombre() + "): Pedido "
                + pedido.getCodigoPedido() + " registrado y pendiente de confirmación de pago.";

        return PedidoResponse.fromEntity(pedido, lineasGuardadas, factura, notificacion);
    }

    // derivar pedido con estado "Pagado" a almacen -> cambia a "En preparacion"
    @Transactional
    public PedidoResponse derivarPedidoAAlmacen(Long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado con ID: " + idPedido));

        if (!"Pagado".equalsIgnoreCase(pedido.getEstado().getNombre())) {
            throw new IllegalArgumentException("Solo se pueden derivar a almacén los pedidos que se encuentran en estado 'Pagado'");
        }

        EstadoPedido estadoEnPreparacion = estadoPedidoRepository.findByNombreIgnoreCase("En preparación")
                .orElseGet(() -> estadoPedidoRepository.findById(3L)
                        .orElseThrow(() -> new NoSuchElementException("Estado 'En preparación' no encontrado en la base de datos")));

        pedido.setEstado(estadoEnPreparacion);
        pedido = pedidoRepository.saveAndFlush(pedido);

        List<LineaPedido> lineas = lineaPedidoRepository.findByPedido_IdPedido(idPedido);
        FacturaElectronica factura = facturaElectronicaRepository.findByPedido_IdPedido(idPedido).orElse(null);
        String notificacion = "El pedido " + pedido.getCodigoPedido()
                + " cambió a 'En preparación' y se notificó al Trabajador de Almacén para el alistamiento de productos.";

        return PedidoResponse.fromEntity(pedido, lineas, factura, notificacion);
    }

    private PedidoResponse construirRespuesta(Pedido p) {
        List<LineaPedido> lineas = lineaPedidoRepository.findByPedido_IdPedido(p.getIdPedido());
        FacturaElectronica factura = facturaElectronicaRepository.findByPedido_IdPedido(p.getIdPedido()).orElse(null);
        return PedidoResponse.fromEntity(p, lineas, factura, null);
    }
}