package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolResponse {
    private Long idRol;
    private String nombre;
    private String descripcion;

    public static RolResponse fromEntity(Rol rol) {
        return RolResponse.builder()
                .idRol(rol.getIdRol())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .build();
    }

    public static List<RolResponse> fromEntities(List<Rol> roles) {
        return roles.stream()
                .map(RolResponse::fromEntity)
                .collect(Collectors.toList());
    }
}