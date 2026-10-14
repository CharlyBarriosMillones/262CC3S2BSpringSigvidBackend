package dsw.sigvidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String telefono;
    private String password;
    private String tipoUsuario; // ej: "cliente", "vendedor", "admin"
    private String nombreRol;   // Ej: "cliente", "administrador"
}