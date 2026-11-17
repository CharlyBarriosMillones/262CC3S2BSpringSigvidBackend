package dsw.sigvidbackend.dto;

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
    private String documentoCliente;
    private String clienteRuc;
    private Long idVendedor;
    private Long idSucursal;
    private String tipoDescuento; // "porcentaje" o "monto_fijo"
    private BigDecimal valorDescuento;
    private Boolean emitirFacturaElectronica;
    private List<LineaPedidoRequest> lineas;
}