package dsw.sigvidbackend.dto;

import jakarta.validation.constraints.*;
import dsw.sigvidbackend.util.Patrones;
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
    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = Patrones.NOMBRE, message = "El nombre solo admite letras (2 a 50)")
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    @Pattern(regexp = Patrones.NOMBRE, message = "El apellido solo admite letras (2 a 50)")
    private String apellido;
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = Patrones.DNI, message = "El DNI debe tener 8 dígitos")
    private String dni;
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo admite máximo 100 caracteres")
    private String email;
    @Pattern(regexp = Patrones.TELEFONO, message = "El teléfono debe tener de 7 a 15 dígitos")
    private String telefono;
    private String tipoUsuario;
    @NotNull(message = "El rol es obligatorio")
    private Long idRol;
    private Long idSucursal;
    @Size(min = 6, max = 64, message = "La contraseña debe tener entre 6 y 64 caracteres")
    private String password;
}