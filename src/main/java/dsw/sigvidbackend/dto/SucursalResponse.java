package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.Sucursal;
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
public class SucursalResponse {
    private Long idSucursal;
    private String nombre;
    private String direccion;
    private String telefono;

    public static SucursalResponse fromEntity(Sucursal sucursal) {
        return SucursalResponse.builder()
                .idSucursal(sucursal.getIdSucursal())
                .nombre(sucursal.getNombre())
                .direccion(sucursal.getDireccion())
                .telefono(sucursal.getTelefono())
                .build();
    }

    public static List<SucursalResponse> fromEntities(List<Sucursal> sucursales) {
        return sucursales.stream()
                .map(SucursalResponse::fromEntity)
                .collect(Collectors.toList());
    }
}