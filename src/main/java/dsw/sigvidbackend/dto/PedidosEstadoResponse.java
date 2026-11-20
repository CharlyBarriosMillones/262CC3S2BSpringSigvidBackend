package dsw.sigvidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// sector del chart polar area "pedidos por estado"
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidosEstadoResponse {
    private String estado;
    private Long cantidad;
}
