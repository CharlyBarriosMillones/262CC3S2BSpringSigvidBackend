package dsw.sigvidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String token;
    private Long idUsuario;
    private String email;
    private String nombre;
    private String apellido;
    private String rol;
    private List<ModuloResponse> modulos;
}