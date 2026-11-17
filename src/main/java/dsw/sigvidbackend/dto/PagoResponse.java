package dsw.sigvidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponse {
    private Long idPago;
    private LocalDateTime fecha;
    private Long idPedido;
    private String codigoPedido;
    private String estadoPedido;
    private String nombreCliente;
    private String metodoPago;
    private BigDecimal montoPedido;
    private BigDecimal montoRecibido;
    private BigDecimal vuelto;
    private String codigoVoucher;
    private Boolean tieneVoucherDigital;
    private BigDecimal nuevoSaldoCaja;
    private String notificacionVendedor;
}