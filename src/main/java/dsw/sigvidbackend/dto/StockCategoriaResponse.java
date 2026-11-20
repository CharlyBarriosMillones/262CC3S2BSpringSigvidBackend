package dsw.sigvidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// eje del chart radar "stock actual vs minimo por categoria"
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockCategoriaResponse {
    private String categoria;
    private Long stockActual;
    private Long stockMinimo;
}
