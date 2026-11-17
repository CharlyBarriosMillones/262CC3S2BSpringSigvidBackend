package dsw.sigvidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movimientos_caja")
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long idMovimiento;

    @Column(name = "fecha", nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "tipo", nullable = false, length = 10)
    @Builder.Default
    private String tipo = "Ingreso";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_caja", referencedColumnName = "id_caja", nullable = false)
    private Caja caja;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pago", referencedColumnName = "id_pago")
    private Pago pago;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cajero", referencedColumnName = "id_usuario", nullable = false)
    private Usuario cajero;
}