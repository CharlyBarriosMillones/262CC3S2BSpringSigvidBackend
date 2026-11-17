package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.Modulo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuloRequest {
    private Integer idMod;
    private String nombre;
    private String descripcion;
    private String url;
    private String icono;
    private Boolean activo;
    private Integer ordenOpcion;

    public static Modulo toEntity(ModuloRequest request) {
        Modulo modulo = new Modulo();
        if (request.getIdMod() != null && request.getIdMod() > 0) {
            modulo.setIdMod(request.getIdMod());
        } else {
            modulo.setIdMod(null);
        }
        modulo.setNombre(request.getNombre());
        modulo.setDescripcion(request.getDescripcion());
        modulo.setUrl(request.getUrl());
        modulo.setIcono(request.getIcono() != null ? request.getIcono() : "bi-grid-fill");
        modulo.setActivo(request.getActivo() != null ? request.getActivo() : true);
        modulo.setOrdenOpcion(request.getOrdenOpcion() != null ? request.getOrdenOpcion() : 1);
        return modulo;
    }
}