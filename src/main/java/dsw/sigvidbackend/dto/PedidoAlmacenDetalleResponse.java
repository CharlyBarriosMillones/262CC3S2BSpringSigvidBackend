package dsw.sigvidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// detalle del pedido para asignar productos (HU08): solicitado vs stock disponible en el almacen
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoAlmacenDetalleResponse {
    private Long idPedido;
    private String codigoPedido;
    private String estado;
    private String nombreCliente;
    private String dniCliente;
    private String nombreSucursal;
    private Long idAlmacen;
    private String nombreAlmacen;
    private List<Linea> lineas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linea {
        private Long idProducto;
        private String codigoProducto;
        private String nombreProducto;
        private Integer cantidadSolicitada;
        private Integer stockDisponible;
        // sugerencia: lo menor entre lo solicitado y lo disponible
        private Integer cantidadSugerida;
        private Boolean stockSuficiente;
    }
}
