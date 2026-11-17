package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponse {
    private Long idUsuario;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String telefono;
    private String tipoUsuario;
    private Long idRol;
    private String nombreRol;
    private Long idSucursal;
    private String fotoPerfil;
    private String preferenciaCategoria;
    private String preferenciaMaridaje;
    private Boolean notifEstadoPedidos;
    private Boolean notifPromocionesEmail;
    private Boolean notifNuevasCosechas;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UsuarioResponse fromEntity(Usuario u) {
        return UsuarioResponse.builder()
                .idUsuario(u.getIdUsuario())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .dni(u.getDni())
                .email(u.getEmail())
                .telefono(u.getTelefono())
                .tipoUsuario(u.getTipoUsuario())
                .idRol(u.getRol() != null ? u.getRol().getIdRol() : null)
                .nombreRol(u.getRol() != null ? u.getRol().getNombre() : null)
                .idSucursal(u.getIdSucursal())
                .fotoPerfil(u.getFotoPerfil())
                .preferenciaCategoria(u.getPreferenciaCategoria() != null ? u.getPreferenciaCategoria() : "Vinos Tintos Gran Reserva")
                .preferenciaMaridaje(u.getPreferenciaMaridaje() != null ? u.getPreferenciaMaridaje() : "Carnes rojas y quesos maduros")
                .notifEstadoPedidos(u.getNotifPedidos() != null ? u.getNotifPedidos() : true)
                .notifPromocionesEmail(u.getNotifPromociones() != null ? u.getNotifPromociones() : true)
                .notifNuevasCosechas(u.getNotifCosechas() != null ? u.getNotifCosechas() : false)
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }

    public static List<UsuarioResponse> fromEntities(List<Usuario> usuarios) {
        return usuarios.stream()
                .map(UsuarioResponse::fromEntity)
                .collect(Collectors.toList());
    }
}