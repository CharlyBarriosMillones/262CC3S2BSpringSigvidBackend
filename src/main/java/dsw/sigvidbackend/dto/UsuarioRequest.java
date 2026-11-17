package dsw.sigvidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioRequest {
    private Long idUsuario;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String telefono;
    private String tipoUsuario;
    private Long idRol;
    private Long idSucursal;
    private String password;
}