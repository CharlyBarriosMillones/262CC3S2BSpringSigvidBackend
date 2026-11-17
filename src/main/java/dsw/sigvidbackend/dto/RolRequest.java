package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.Rol;
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
    private String nombre;
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