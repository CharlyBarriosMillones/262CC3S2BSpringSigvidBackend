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
public class PerfilUpdateRequest {
    private String email;
    @Pattern(regexp = Patrones.NOMBRE, message = "El nombre solo admite letras (2 a 50)")
    private String nombre;
    @Pattern(regexp = Patrones.NOMBRE, message = "El apellido solo admite letras (2 a 50)")
    private String apellido;
    @Pattern(regexp = Patrones.TELEFONO, message = "El teléfono debe tener de 7 a 15 dígitos")
    private String telefono;
    private String fotoPerfil;
    @Size(max = 100, message = "La preferencia de categoría admite máximo 100 caracteres")
    @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "La preferencia contiene caracteres no permitidos")
    private String preferenciaCategoria;
    @Size(max = 150, message = "La preferencia de maridaje admite máximo 150 caracteres")
    @Pattern(regexp = Patrones.TEXTO_SEGURO, message = "La preferencia contiene caracteres no permitidos")
    private String preferenciaMaridaje;
    private Boolean notifEstadoPedidos;
    private Boolean notifPromocionesEmail;
    private Boolean notifNuevasCosechas;
}