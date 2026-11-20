package dsw.sigvidbackend.dto;

import jakarta.validation.constraints.*;
import dsw.sigvidbackend.util.Patrones;
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
    @NotNull(message = "El pedido es obligatorio")
    private Long idPedido;
    private Long idCajero;
    private Long idMetodo;
    @Pattern(regexp = Patrones.METODO_PAGO, message = "El método de pago debe ser Efectivo, Tarjeta o Transferencia")
    private String nombreMetodo; // "efectivo", "tarjeta", "transferencia"
    private Long idCaja;
    @DecimalMin(value = "0", message = "El monto recibido no puede ser negativo")
    private BigDecimal montoRecibido; // para efectivo: calcula el vuelto
    @Pattern(regexp = Patrones.CODIGO_VOUCHER, message = "El código de voucher solo admite letras, números y guiones (4 a 30)")
    private String codigoVoucher;     // para tarjeta / transferencia
    // ~2 MB de archivo en base64
    @Size(max = 2_900_000, message = "El voucher digital no debe superar los 2 MB")
    @Pattern(regexp = Patrones.VOUCHER_ARCHIVO, message = "El voucher debe ser una imagen PNG/JPG o un PDF en base64")
    private String voucherBase64;     // imagen o pdf del voucher en Base64
}