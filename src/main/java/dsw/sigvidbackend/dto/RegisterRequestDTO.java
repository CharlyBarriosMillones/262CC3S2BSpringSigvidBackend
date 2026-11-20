package dsw.sigvidbackend.dto;

import jakarta.validation.constraints.*;
import dsw.sigvidbackend.util.Patrones;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
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
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 64, message = "La contraseña debe tener entre 6 y 64 caracteres")
    private String password;
    private String tipoUsuario; // ej: "cliente", "vendedor", "admin"
    private String nombreRol;   // Ej: "cliente", "administrador"

    // segun el tipo de usuario
    @Pattern(regexp = Patrones.RUC, message = "El RUC debe tener 11 dígitos y empezar en 10, 15, 17 o 20")
    private String ruc;            // Cliente
    @Size(max = 150, message = "La razón social admite máximo 150 caracteres")
    @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "La razón social contiene caracteres no permitidos")
    private String razonSocial;    // Cliente
    @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "La dirección contiene caracteres no permitidos")
    private String direccion;      // Cliente
    private String nivel;          // Administrador ("SuperAdmin" o "General")
    private String area;           // Vendedor
    private String cajaAsignada;   // Cajero
    private String ubicacionSede;  // Almacenero
}