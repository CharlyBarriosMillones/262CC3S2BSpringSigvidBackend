package dsw.sigvidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trabajadores_almacen")
public class TrabajadorAlmacen {

    @Id
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "ubicacion_sede", length = 100)
    private String ubicacionSede;
}