package dsw.sigvidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vendedores")
public class Vendedor {

    @Id
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "area", length = 50)
    private String area;
}