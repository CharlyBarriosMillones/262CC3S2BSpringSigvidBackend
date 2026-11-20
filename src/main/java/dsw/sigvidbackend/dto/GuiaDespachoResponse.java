package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.DetalleGuia;
import dsw.sigvidbackend.model.GuiaDespacho;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// guia de despacho con su pedido y sus items (HU08, HU09, HU10)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaDespachoResponse {
    private Long idGuia;
    private String codigoGuia;
    private String estadoGuia;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaSalida;
    private Long idAlmacen;
    private String nombreAlmacen;
    private String nombreTrabajador;
    private Long idPedido;
    private String codigoPedido;
    private String estadoPedido;
    private String nombreCliente;
    private String dniCliente;
    private Integer totalItems;
    private Integer itemsEscaneados;
    private String notificacion;
    private List<DetalleGuiaResponse> detalles;

    public static GuiaDespachoResponse fromEntity(GuiaDespacho g, List<DetalleGuia> detalles, String notificacion) {
        var p = g.getPedido();
        int escaneados = (int) detalles.stream().filter(d -> Boolean.TRUE.equals(d.getEscaneado())).count();
        return GuiaDespachoResponse.builder()
                .idGuia(g.getIdGuia())
                .codigoGuia(g.getCodigoGuia())
                .estadoGuia(g.getEstado())
                .fechaGeneracion(g.getFechaGeneracion())
                .fechaSalida(g.getFechaSalida())
                .idAlmacen(g.getAlmacen().getIdAlmacen())
                .nombreAlmacen(g.getAlmacen().getNombre())
                .nombreTrabajador(g.getTrabajador().getNombre() + " " + g.getTrabajador().getApellido())
                .idPedido(p.getIdPedido())
                .codigoPedido(p.getCodigoPedido())
                .estadoPedido(p.getEstado().getNombre())
                .nombreCliente(p.getCliente().getNombre() + " " + p.getCliente().getApellido())
                .dniCliente(p.getCliente().getDni())
                .totalItems(detalles.size())
                .itemsEscaneados(escaneados)
                .notificacion(notificacion)
                .detalles(DetalleGuiaResponse.fromEntities(detalles))
                .build();
    }
}
