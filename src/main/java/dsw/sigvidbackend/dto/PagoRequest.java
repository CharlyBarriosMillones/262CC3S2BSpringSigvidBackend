package dsw.sigvidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoRequest {
    private Long idPedido;
    private Long idCajero;
    private Long idMetodo;
    private String nombreMetodo; // "efectivo", "tarjeta", "transferencia"
    private Long idCaja;
    private BigDecimal montoRecibido; // para efectivo: calcula el vuelto
    private String codigoVoucher;     // para tarjeta / transferencia
    private String voucherBase64;     // imagen o pdf del voucher en Base64
}