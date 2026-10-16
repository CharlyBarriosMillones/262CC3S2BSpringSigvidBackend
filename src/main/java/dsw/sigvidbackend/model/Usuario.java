package dsw.sigvidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 50)
    private String apellido;

    @Column(name = "dni", length = 8)
    private String dni;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "tipo_usuario", length = 20)
    private String tipoUsuario;

    // relacion con Rol
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", referencedColumnName = "id_rol")
    private Rol rol;

    @Column(name = "id_sucursal")
    private Long idSucursal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // foto de perfil, preferencias y notificaciones por email
    @Column(name = "foto_perfil", columnDefinition = "TEXT")
    private String fotoPerfil;

    @Column(name = "preferencia_categoria", length = 100)
    private String preferenciaCategoria = "Vinos Tintos Gran Reserva";

    @Column(name = "preferencia_maridaje", length = 150)
    private String preferenciaMaridaje = "Carnes rojas y quesos maduros";

    @Column(name = "notif_pedidos")
    private Boolean notifPedidos = true;

    @Column(name = "notif_promociones")
    private Boolean notifPromociones = true;

    @Column(name = "notif_cosechas")
    private Boolean notifCosechas = false;
}