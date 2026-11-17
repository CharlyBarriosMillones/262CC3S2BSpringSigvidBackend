package dsw.sigvidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "modulos")
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mod")
    private Integer idMod;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(nullable = false, length = 120)
    private String url;

    @Column(nullable = false, length = 60)
    @Builder.Default
    private String icono = "bi-grid-fill";

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "orden_opcion", nullable = false)
    @Builder.Default
    private Integer ordenOpcion = 1;
}