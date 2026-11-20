package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.Almacen;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// almacen para combos de filtro (HU07, HU08)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlmacenResponse {
    private Long idAlmacen;
    private String nombre;
    private String direccion;
    private String idUbigeo;
    private String departamento;
    private String provincia;
    private String distrito;

    public static AlmacenResponse fromEntity(Almacen a) {
        var u = a.getUbigeo();
        return AlmacenResponse.builder()
                .idAlmacen(a.getIdAlmacen())
                .nombre(a.getNombre())
                .direccion(a.getDireccion())
                .idUbigeo(u != null ? u.getIdUbigeo() : null)
                .departamento(u != null ? u.getDepartamento() : null)
                .provincia(u != null ? u.getProvincia() : null)
                .distrito(u != null ? u.getDistrito() : null)
                .build();
    }

    public static List<AlmacenResponse> fromEntities(List<Almacen> lista) {
        return lista.stream().map(AlmacenResponse::fromEntity).toList();
    }
}
