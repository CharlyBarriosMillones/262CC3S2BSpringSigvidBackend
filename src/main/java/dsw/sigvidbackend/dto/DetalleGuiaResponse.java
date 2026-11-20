package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.DetalleGuia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// item de la guia de despacho (HU08, HU09, HU10)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleGuiaResponse {
    private Long idDetalle;
    private Long idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private Integer cantidadSolicitada;
    private Integer cantidadAsignada;
    private String codigoQr;
    private Boolean escaneado;
    private Integer cantidadRecibida;

    public static DetalleGuiaResponse fromEntity(DetalleGuia d) {
        return DetalleGuiaResponse.builder()
                .idDetalle(d.getIdDetalle())
                .idProducto(d.getProducto().getIdProducto())
                .codigoProducto(d.getProducto().getCodigoProducto())
                .nombreProducto(d.getProducto().getNombre())
                .cantidadSolicitada(d.getCantidadSolicitada())
                .cantidadAsignada(d.getCantidadAsignada())
                .codigoQr(d.getCodigoQr())
                .escaneado(d.getEscaneado())
                .cantidadRecibida(d.getCantidadRecibida())
                .build();
    }

    public static List<DetalleGuiaResponse> fromEntities(List<DetalleGuia> lista) {
        return lista.stream().map(DetalleGuiaResponse::fromEntity).toList();
    }
}
