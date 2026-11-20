package dsw.sigvidbackend.dto;

import jakarta.validation.constraints.*;
import dsw.sigvidbackend.util.Patrones;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {
    private Long idProducto;
    @NotBlank(message = "El código del producto es obligatorio")
    @Pattern(regexp = Patrones.CODIGO_PRODUCTO, message = "El código debe tener el formato AAA-000 (ej. VIN-001)")
    private String codigoProducto;
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 150, message = "El nombre admite máximo 150 caracteres")
    @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "El nombre contiene caracteres no permitidos")
    private String nombre;
    @NotNull(message = "El precio referencial es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal precioReferencial;
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;
    @NotNull(message = "La categoría es obligatoria")
    private Long idCategoria;
    @NotNull(message = "La marca es obligatoria")
    private Long idMarca;
    private Long idProveedor;
    private String cepa;
    private String descripcion;
    private String maridaje;
    private String imagenUrl;
    private Boolean activo;
}