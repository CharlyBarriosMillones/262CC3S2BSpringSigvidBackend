package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.Sucursal;
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
    private String nombre;
    private String direccion;
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