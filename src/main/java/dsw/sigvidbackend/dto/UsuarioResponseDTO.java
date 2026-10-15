package dsw.sigvidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponseDTO {
    private Long idUsuario;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String telefono;
    private String tipoUsuario;
    private Long idRol;
    private String nombreRol;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}