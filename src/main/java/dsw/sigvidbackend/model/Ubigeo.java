package dsw.sigvidbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ubicacion geografica (departamento / provincia / distrito), igual que en el proyecto base
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ubigeo")
public class Ubigeo {
    @Id
    @Column(name = "idubigeo", length = 6)
    private String idUbigeo;

    @Column(name = "departamento", length = 50)
    private String departamento;

    @Column(name = "provincia", length = 50)
    private String provincia;

    @Column(name = "distrito", length = 50)
    private String distrito;
}
