package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.util.Patrones;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// confirmacion de recepcion del pedido por el cliente (HU10)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecepcionRequest {
    @NotBlank(message = "El tipo de confirmación es obligatorio")
    @Pattern(regexp = Patrones.TIPO_RECEPCION, message = "El tipo de confirmación debe ser CONFORMIDAD o FIRMA")
    private String tipo;

    // obligatoria si tipo = FIRMA: imagen png en base64 (data URL) dibujada en pantalla
    @Size(max = 700_000, message = "La firma digital no debe superar los 500 KB")
    @Pattern(regexp = Patrones.FIRMA_PNG, message = "La firma digital debe ser una imagen PNG en base64")
    private String firmaDigital;

    @Size(max = 300, message = "Las observaciones admiten máximo 300 caracteres")
    @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "Las observaciones contienen caracteres no permitidos")
    private String observaciones;

    @NotEmpty(message = "Debe verificar las cantidades recibidas")
    private List<@Valid Linea> lineas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linea {
        @NotNull(message = "El item de la guía es obligatorio")
        private Long idDetalle;

        @NotNull(message = "La cantidad recibida es obligatoria")
        @Min(value = 0, message = "La cantidad recibida no puede ser negativa")
        @Max(value = 10000, message = "La cantidad recibida máxima es 10000")
        private Integer cantidadRecibida;
    }
}
