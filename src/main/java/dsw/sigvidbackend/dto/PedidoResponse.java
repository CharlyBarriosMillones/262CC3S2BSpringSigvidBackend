package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.FacturaElectronica;
import dsw.sigvidbackend.model.LineaPedido;
import dsw.sigvidbackend.model.Pedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponse {
    private Long idPedido;
    private String codigoPedido;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaEntrega;
    private BigDecimal subtotalBruto;
    private BigDecimal descuento;
    private BigDecimal importeTotal;
    private Long idEstado;
    private String estado;
    private Long idSucursal;
    private String nombreSucursal;
    private Long idCliente;
    private String nombreCliente;
    private String dniCliente;
    private Long idVendedor;
    private String nombreVendedor;
    private String codigoFactura;
    private String clienteRuc;
    private String sunatEstado;
    private String notificacionAlmacen;
    private List<LineaPedidoResponse> lineas;

    public static PedidoResponse fromEntity(Pedido p, List<LineaPedido> lineas, FacturaElectronica factura, String notificacion) {
        List<LineaPedidoResponse> lineasDto = LineaPedidoResponse.fromEntities(lineas);
        BigDecimal bruto = lineas.stream()
                .map(LineaPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PedidoResponse.builder()
                .idPedido(p.getIdPedido())
                .codigoPedido(p.getCodigoPedido())
                .fechaSolicitud(p.getFechaSolicitud())
                .fechaPago(p.getFechaPago())
                .fechaEntrega(p.getFechaEntrega())
                .subtotalBruto(bruto)
                .descuento(p.getDescuento())
                .importeTotal(p.getImporteTotal())
                .idEstado(p.getEstado() != null ? p.getEstado().getIdEstado() : null)
                .estado(p.getEstado() != null ? p.getEstado().getNombre() : null)
                .idSucursal(p.getSucursal() != null ? p.getSucursal().getIdSucursal() : null)
                .nombreSucursal(p.getSucursal() != null ? p.getSucursal().getNombre() : null)
                .idCliente(p.getCliente() != null ? p.getCliente().getIdUsuario() : null)
                .nombreCliente(p.getCliente() != null ? p.getCliente().getNombre() + " " + p.getCliente().getApellido() : null)
                .dniCliente(p.getCliente() != null ? p.getCliente().getDni() : null)
                .idVendedor(p.getVendedor() != null ? p.getVendedor().getIdUsuario() : null)
                .nombreVendedor(p.getVendedor() != null ? p.getVendedor().getNombre() + " " + p.getVendedor().getApellido() : null)
                .codigoFactura(factura != null ? factura.getCodigoFactura() : null)
                .clienteRuc(factura != null ? factura.getClienteRuc() : null)
                .sunatEstado(factura != null ? factura.getSunatEstado() : null)
                .notificacionAlmacen(notificacion)
                .lineas(lineasDto)
                .build();
    }
}