package dsw.sigvidbackend.dto;

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
    private String nombre;
    private String apellido;
    private String telefono;
    private String fotoPerfil;
    private String preferenciaCategoria;
    private String preferenciaMaridaje;
    private Boolean notifEstadoPedidos;
    private Boolean notifPromocionesEmail;
    private Boolean notifNuevasCosechas;
}