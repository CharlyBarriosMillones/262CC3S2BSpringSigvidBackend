package dsw.sigvidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cajas")
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_caja")
    private Long idCaja;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "saldo_actual", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal saldoActual = BigDecimal.ZERO;
}