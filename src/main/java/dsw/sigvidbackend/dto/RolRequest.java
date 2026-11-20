package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.Rol;
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
public class RolRequest {
    private Long idRol;
    @NotBlank(message = "El nombre del rol es obligatorio")
    @Pattern(regexp = Patrones.NOMBRE, message = "El nombre del rol solo admite letras (2 a 50)")
    private String nombre;
    @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "La descripción contiene caracteres no permitidos")
    private String descripcion;

    public static Rol toEntity(RolRequest request) {
        Rol rol = new Rol();
        if (request.getIdRol() != null && request.getIdRol() > 0) {
            rol.setIdRol(request.getIdRol());
        } else {
            rol.setIdRol(null);
        }
        rol.setNombre(request.getNombre());
        rol.setDescripcion(request.getDescripcion());
        return rol;
    }
}