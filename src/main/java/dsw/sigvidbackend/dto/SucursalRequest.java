package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.Sucursal;
import jakarta.validation.constraints.*;
import dsw.sigvidbackend.util.Patrones;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SucursalRequest {
    private Long idSucursal;
    @NotBlank(message = "El nombre de la sucursal es obligatorio")
    @Size(max = 100, message = "El nombre admite máximo 100 caracteres")
    @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "El nombre contiene caracteres no permitidos")
    private String nombre;
    @NotBlank(message = "La dirección es obligatoria")
    @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "La dirección contiene caracteres no permitidos")
    private String direccion;
    @Pattern(regexp = Patrones.TELEFONO, message = "El teléfono debe tener de 7 a 15 dígitos")
    private String telefono;

    public static Sucursal toEntity(SucursalRequest request) {
        Sucursal sucursal = new Sucursal();
        if (request.getIdSucursal() != null && request.getIdSucursal() > 0) {
            sucursal.setIdSucursal(request.getIdSucursal());
        } else {
            sucursal.setIdSucursal(null);
        }
        sucursal.setNombre(request.getNombre());
        sucursal.setDireccion(request.getDireccion());
        sucursal.setTelefono(request.getTelefono());
        return sucursal;
    }
}