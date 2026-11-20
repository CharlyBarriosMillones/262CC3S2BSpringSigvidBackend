package dsw.sigvidbackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import dsw.sigvidbackend.util.Patrones;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequest {
    private Long idCliente;
    @Pattern(regexp = Patrones.DNI_O_RUC, message = "El documento del cliente debe ser un DNI de 8 dígitos o un RUC de 11 dígitos")
    private String documentoCliente;
    @Pattern(regexp = Patrones.RUC, message = "El RUC debe tener 11 dígitos y empezar en 10, 15, 17 o 20")
    private String clienteRuc;
    private Long idVendedor;
    private Long idSucursal;
    @Pattern(regexp = Patrones.TIPO_DESCUENTO, message = "El tipo de descuento debe ser PORCENTAJE o MONTO_FIJO")
    private String tipoDescuento; // "porcentaje" o "monto_fijo"
    @DecimalMin(value = "0", message = "El descuento no puede ser negativo")
    private BigDecimal valorDescuento;
    private Boolean emitirFacturaElectronica;
    @NotEmpty(message = "El pedido debe incluir al menos un producto")
    private List<@Valid LineaPedidoRequest> lineas;
}