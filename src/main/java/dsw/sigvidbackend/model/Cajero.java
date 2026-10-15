package dsw.sigvidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cajeros")
public class Cajero {

    @Id
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "caja_asignada", length = 30)
    private String cajaAsignada;
}