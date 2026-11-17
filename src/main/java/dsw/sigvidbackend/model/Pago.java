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
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @Column(name = "fecha", nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_metodo", referencedColumnName = "id_metodo", nullable = false)
    private MetodoPago metodoPago;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pedido", referencedColumnName = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cajero", referencedColumnName = "id_usuario", nullable = false)
    private Usuario cajero;
}