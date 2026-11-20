package dsw.sigvidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// confirmacion de recepcion del pedido por el cliente: conformidad (un clic) o firma digital (HU10)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recepciones_pedido")
public class RecepcionPedido {
    public static final String CONFORMIDAD = "CONFORMIDAD";
    public static final String FIRMA = "FIRMA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recepcion")
    private Long idRecepcion;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pedido", referencedColumnName = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    // imagen de la firma dibujada en pantalla (data URL base64)
    @Column(name = "firma_digital", columnDefinition = "TEXT")
    private String firmaDigital;

    @Column(name = "observaciones", length = 300)
    private String observaciones;

    @Column(name = "fecha", nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();
}
