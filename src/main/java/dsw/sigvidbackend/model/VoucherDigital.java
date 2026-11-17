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
@Table(name = "vouchers_digitales")
public class VoucherDigital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_voucher")
    private Long idVoucher;

    @Column(name = "codigo_voucher", nullable = false, length = 30)
    private String codigoVoucher;

    @Column(name = "fecha", nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "imagen_digital")
    private byte[] imagenDigital;

    @Column(name = "tipo_pago", length = 20)
    private String tipoPago;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pago", referencedColumnName = "id_pago", nullable = false)
    private Pago pago;

    @Column(name = "id_cuenta")
    private Long idCuenta;
}