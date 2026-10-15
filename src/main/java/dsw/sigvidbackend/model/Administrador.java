package dsw.sigvidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "administradores")
public class Administrador {

    @Id
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "nivel", nullable = false, length = 20)
    private String nivel;
}