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

    // segun el tipo de usuario
    private String ruc;            // Cliente
    private String razonSocial;    // Cliente
    private String direccion;      // Cliente
    private String nivel;          // Administrador ("SuperAdmin" o "General")
    private String area;           // Vendedor
    private String cajaAsignada;   // Cajero
    private String ubicacionSede;  // Almacenero
}