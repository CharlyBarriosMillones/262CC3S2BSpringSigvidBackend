package dsw.sigvidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// guia de despacho de un pedido: "Preliminar" al asignar (HU08), "Despachada" al dar salida (HU09)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "guias_despacho")
public class GuiaDespacho {
    public static final String PRELIMINAR = "Preliminar";
    public static final String DESPACHADA = "Despachada";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_guia")
    private Long idGuia;

    @Column(name = "codigo_guia", nullable = false, unique = true, length = 20)
    private String codigoGuia;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pedido", referencedColumnName = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_almacen", referencedColumnName = "id_almacen", nullable = false)
    private Almacen almacen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_trabajador", referencedColumnName = "id_usuario", nullable = false)
    private Usuario trabajador;

    @Column(name = "fecha_generacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida;

    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private String estado = PRELIMINAR;
}
