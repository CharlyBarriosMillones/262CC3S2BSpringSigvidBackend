package dsw.sigvidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// stock actual de un producto en un almacen (HU07, HU09)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock", uniqueConstraints = @UniqueConstraint(columnNames = {"id_producto", "id_almacen"}))
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stock")
    private Long idStock;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_almacen", referencedColumnName = "id_almacen", nullable = false)
    private Almacen almacen;

    @Column(name = "cantidad_actual", nullable = false)
    @Builder.Default
    private Integer cantidadActual = 0;

    // ultima vez que un trabajador de almacen confirmo la disponibilidad ("Confirmar stock")
    @Column(name = "fecha_ultima_confirmacion")
    private LocalDateTime fechaUltimaConfirmacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario_confirma", referencedColumnName = "id_usuario")
    private Usuario usuarioConfirma;
}
