package dsw.sigvidbackend.dto;

import jakarta.validation.constraints.*;
import dsw.sigvidbackend.util.Patrones;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecuperarPasswordRequestDTO {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String email;
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = Patrones.DNI, message = "El DNI debe tener 8 dígitos")
    private String dni;
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 6, max = 64, message = "La contraseña debe tener entre 6 y 64 caracteres")
    private String nuevaPassword;
}