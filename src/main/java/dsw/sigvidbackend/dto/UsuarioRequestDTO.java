package dsw.sigvidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioRequestDTO {
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String telefono;
    private String tipoUsuario;
    private Long idRol;
    private String password; // es opcional al actualizar pero requerido al crear
}