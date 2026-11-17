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
@Table(name = "lineas_pedido")
public class LineaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_linea")
    private Long idLinea;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "descuento_linea", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal descuentoLinea = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", referencedColumnName = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto", nullable = false)
    private Producto producto;
}