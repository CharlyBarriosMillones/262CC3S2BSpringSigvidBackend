package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.LineaPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineaPedidoResponse {
    private Long idLinea;
    private Long idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal descuentoLinea;
    private BigDecimal subtotal;

    public static LineaPedidoResponse fromEntity(LineaPedido lp) {
        return LineaPedidoResponse.builder()
                .idLinea(lp.getIdLinea())
                .idProducto(lp.getProducto() != null ? lp.getProducto().getIdProducto() : null)
                .codigoProducto(lp.getProducto() != null ? lp.getProducto().getCodigoProducto() : null)
                .nombreProducto(lp.getProducto() != null ? lp.getProducto().getNombre() : null)
                .cantidad(lp.getCantidad())
                .precioUnitario(lp.getPrecioUnitario())
                .descuentoLinea(lp.getDescuentoLinea())
                .subtotal(lp.getSubtotal())
                .build();
    }

    public static List<LineaPedidoResponse> fromEntities(List<LineaPedido> lineas) {
        return lineas.stream()
                .map(LineaPedidoResponse::fromEntity)
                .collect(Collectors.toList());
    }
}