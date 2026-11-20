package dsw.sigvidbackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// asignacion de productos a un pedido (HU08)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionRequest {
    @NotNull(message = "El almacén es obligatorio")
    private Long idAlmacen;

    @NotEmpty(message = "Debe indicar las cantidades a asignar")
    private List<@Valid Linea> lineas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linea {
        @NotNull(message = "El producto es obligatorio")
        private Long idProducto;

        @NotNull(message = "La cantidad asignada es obligatoria")
        @Min(value = 0, message = "La cantidad asignada no puede ser negativa")
        @Max(value = 10000, message = "La cantidad asignada máxima es 10000")
        private Integer cantidadAsignada;
    }
}
