package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.Modulo;
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
public class ModuloResponse {
    private Integer idMod;
    private String nombre;
    private String descripcion;
    private String url;
    private String icono;
    private Boolean activo;
    private Integer ordenOpcion;

    public static ModuloResponse fromEntity(Modulo modulo) {
        return ModuloResponse.builder()
                .idMod(modulo.getIdMod())
                .nombre(modulo.getNombre())
                .descripcion(modulo.getDescripcion())
                .url(modulo.getUrl())
                .icono(modulo.getIcono())
                .activo(modulo.getActivo())
                .ordenOpcion(modulo.getOrdenOpcion())
                .build();
    }

    public static List<ModuloResponse> fromEntities(List<Modulo> modulos) {
        return modulos.stream()
                .map(ModuloResponse::fromEntity)
                .collect(Collectors.toList());
    }
}