package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.PagoRequest;
import dsw.sigvidbackend.dto.PagoResponse;
import dsw.sigvidbackend.model.*;
import dsw.sigvidbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final CajaRepository cajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final VoucherDigitalRepository voucherDigitalRepository;
    private final UsuarioRepository usuarioRepository;
    private final CajeroRepository cajeroRepository;

    public List<MetodoPago> listarMetodosPago() {
        return metodoPagoRepository.findAll();
    }

    public List<Caja> listarCajas() {
        List<Caja> cajas = cajaRepository.findAll();
        if (cajas.isEmpty()) {
            Caja cajaDefault = cajaRepository.saveAndFlush(
                    Caja.builder()
                            .nombre("Caja 1 - Principal")
                            .saldoActual(new BigDecimal("1500.00"))
                            .build()
            );
            return List.of(cajaDefault);
        }
        return cajas;
    }

    public List<PagoResponse> listarPagos() {
        return pagoRepository.findAllByOrderByFechaDesc().stream()
                .map(p -> {
                    VoucherDigital voucher = voucherDigitalRepository.findByPago_IdPago(p.getIdPago()).orElse(null);
                    return PagoResponse.builder()
                            .idPago(p.getIdPago())
                            .fecha(p.getFecha())
                            .idPedido(p.getPedido().getIdPedido())
                            .codigoPedido(p.getPedido().getCodigoPedido())
                            .estadoPedido(p.getPedido().getEstado().getNombre())
                            .nombreCliente(p.getPedido().getCliente().getNombre() + " " + p.getPedido().getCliente().getApellido())
                            .metodoPago(p.getMetodoPago().getNombre())
                            .montoPedido(p.getMonto())
                            .montoRecibido(p.getMonto())
                            .vuelto(BigDecimal.ZERO)
                            .codigoVoucher(voucher != null ? voucher.getCodigoVoucher() : null)
                            .tieneVoucherDigital(voucher != null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public PagoResponse registrarPago(PagoRequest request) {
        if (request.getIdPedido() == null) {
            throw new IllegalArgumentException("El ID del pedido es obligatorio para registrar el pago");
        }

        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado con ID: " + request.getIdPedido()));

        if ("Pagado".equalsIgnoreCase(pedido.getEstado().getNombre())) {
            throw new IllegalArgumentException("El pedido " + pedido.getCodigoPedido() + " ya se encuentra pagado");
        }

        // obtener metodo de pago (efectivo, tarjeta, transferencia)
        MetodoPago metodo;
        if (request.getIdMetodo() != null) {
            metodo = metodoPagoRepository.findById(request.getIdMetodo())
                    .orElseThrow(() -> new NoSuchElementException("Método de pago no encontrado con ID: " + request.getIdMetodo()));
        } else if (request.getNombreMetodo() != null && !request.getNombreMetodo().isBlank()) {
            metodo = metodoPagoRepository.findByNombreIgnoreCase(request.getNombreMetodo().trim())
                    .orElseThrow(() -> new NoSuchElementException("Método de pago no encontrado: " + request.getNombreMetodo()));
        } else {
            metodo = metodoPagoRepository.findById(1L)
                    .orElseThrow(() -> new NoSuchElementException("Método de pago por defecto no encontrado"));
        }

        // obtener cajero y asegurar registro en tabla cajeros 
        Usuario cajero = request.getIdCajero() != null
                ? usuarioRepository.findById(request.getIdCajero()).orElse(pedido.getVendedor())
                : pedido.getVendedor();

        if (!cajeroRepository.existsById(cajero.getIdUsuario())) {
            cajeroRepository.saveAndFlush(new Cajero(cajero.getIdUsuario(), "Caja 1 - Principal"));
        }

        // validar monto recibido y calcular vuelto si es efectivo
        BigDecimal montoPedido = pedido.getImporteTotal();
        BigDecimal montoRecibido = request.getMontoRecibido() != null ? request.getMontoRecibido() : montoPedido;
        BigDecimal vuelto = BigDecimal.ZERO;

        if ("Efectivo".equalsIgnoreCase(metodo.getNombre())) {
            if (montoRecibido.compareTo(montoPedido) < 0) {
                throw new IllegalArgumentException("El monto recibido en efectivo (S/ " + montoRecibido
                        + ") es menor al total del pedido (S/ " + montoPedido + ")");
            }
            vuelto = montoRecibido.subtract(montoPedido).setScale(2, RoundingMode.HALF_UP);
        } else {
            montoRecibido = montoPedido;
        }

        // registrar pago
        Pago pago = Pago.builder()
                .fecha(LocalDateTime.now())
                .monto(montoPedido)
                .metodoPago(metodo)
                .pedido(pedido)
                .cajero(cajero)
                .build();
        pago = pagoRepository.saveAndFlush(pago);

        // si es tarjeta o transferencia, guardar voucher digital (imagen/pdf)
        String codigoVoucherGenerado = null;
        boolean tieneVoucher = false;
        if (!"Efectivo".equalsIgnoreCase(metodo.getNombre())) {
            codigoVoucherGenerado = (request.getCodigoVoucher() != null && !request.getCodigoVoucher().isBlank())
                    ? request.getCodigoVoucher().trim()
                    : String.format("VOU-%05d", pago.getIdPago());

            byte[] bytesVoucher = null;
            if (request.getVoucherBase64() != null && !request.getVoucherBase64().isBlank()) {
                String limpio = request.getVoucherBase64().contains(",")
                        ? request.getVoucherBase64().split(",")[1]
                        : request.getVoucherBase64();
                try {
                    bytesVoucher = Base64.getDecoder().decode(limpio);
                } catch (IllegalArgumentException e) {
                    bytesVoucher = request.getVoucherBase64().getBytes(StandardCharsets.UTF_8);
                }
            }

            VoucherDigital voucher = VoucherDigital.builder()
                    .codigoVoucher(codigoVoucherGenerado)
                    .fecha(LocalDateTime.now())
                    .monto(montoPedido)
                    .imagenDigital(bytesVoucher)
                    .tipoPago(metodo.getNombre())
                    .pago(pago)
                    .build();
            voucherDigitalRepository.saveAndFlush(voucher);
            tieneVoucher = true;
        }

        // actualizar estado del pedido a "pagado"
        EstadoPedido estadoPagado = estadoPedidoRepository.findByNombreIgnoreCase("Pagado")
                .orElseGet(() -> estadoPedidoRepository.findById(2L)
                        .orElseThrow(() -> new NoSuchElementException("Estado 'Pagado' no encontrado")));

        pedido.setEstado(estadoPagado);
        pedido.setFechaPago(LocalDateTime.now());
        pedidoRepository.saveAndFlush(pedido);

        // actualizar saldo de caja y registrar MovimientoCaja
        List<Caja> cajas = listarCajas();
        Caja caja = request.getIdCaja() != null
                ? cajaRepository.findById(request.getIdCaja()).orElse(cajas.get(0))
                : cajas.get(0);

        BigDecimal nuevoSaldo = caja.getSaldoActual().add(montoPedido).setScale(2, RoundingMode.HALF_UP);
        caja.setSaldoActual(nuevoSaldo);
        cajaRepository.saveAndFlush(caja);

        MovimientoCaja movimiento = MovimientoCaja.builder()
                .fecha(LocalDateTime.now())
                .monto(montoPedido)
                .tipo("Ingreso")
                .caja(caja)
                .pago(pago)
                .cajero(cajero)
                .build();
        movimientoCajaRepository.saveAndFlush(movimiento);

        String notificacionVendedor = "Pago confirmado para el pedido " + pedido.getCodigoPedido()
                + ". El Vendedor ya puede derivarlo a Almacén desde su panel.";

        return PagoResponse.builder()
                .idPago(pago.getIdPago())
                .fecha(pago.getFecha())
                .idPedido(pedido.getIdPedido())
                .codigoPedido(pedido.getCodigoPedido())
                .estadoPedido(estadoPagado.getNombre())
                .nombreCliente(pedido.getCliente().getNombre() + " " + pedido.getCliente().getApellido())
                .metodoPago(metodo.getNombre())
                .montoPedido(montoPedido)
                .montoRecibido(montoRecibido)
                .vuelto(vuelto)
                .codigoVoucher(codigoVoucherGenerado)
                .tieneVoucherDigital(tieneVoucher)
                .nuevoSaldoCaja(nuevoSaldo)
                .notificacionVendedor(notificacionVendedor)
                .build();
    }
}