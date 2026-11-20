package dsw.sigvidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// punto del chart de lineas "ventas por dia" (monto cobrado y numero de pedidos pagados)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaDiariaResponse {
    private LocalDate fecha;
    private BigDecimal total;
    private Long pedidos;
}
