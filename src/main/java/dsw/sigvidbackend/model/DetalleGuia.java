package dsw.sigvidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// item de la guia de despacho: solicitado, asignado (HU08), escaneado por QR (HU09) y recibido (HU10)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "detalles_guia")
public class DetalleGuia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_guia", referencedColumnName = "id_guia", nullable = false)
    private GuiaDespacho guia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad_solicitada", nullable = false)
    private Integer cantidadSolicitada;

    @Column(name = "cantidad_asignada", nullable = false)
    private Integer cantidadAsignada;

    // contenido del codigo QR que se muestra en pantalla para este item
    @Column(name = "codigo_qr", nullable = false, length = 120)
    private String codigoQr;

    @Column(name = "escaneado", nullable = false)
    @Builder.Default
    private Boolean escaneado = false;

    @Column(name = "cantidad_recibida")
    private Integer cantidadRecibida;
}
