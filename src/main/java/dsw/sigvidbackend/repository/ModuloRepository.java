package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Integer> {

    @Query("SELECT m FROM Modulo m JOIN RolModulo rm ON m.idMod = rm.idMod " +
           "WHERE m.activo = true AND rm.activo = true AND rm.idRol = :idRol " +
           "ORDER BY m.ordenOpcion ASC")
    List<Modulo> findByRolId(@Param("idRol") Long idRol);

    @Query("SELECT m FROM Modulo m WHERE m.activo = true " +
           "AND (:descripcion IS NULL OR LOWER(m.descripcion) LIKE LOWER(CONCAT('%', :descripcion, '%')) " +
           "OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :descripcion, '%'))) " +
           "ORDER BY m.ordenOpcion ASC")
    List<Modulo> findByFilter(@Param("descripcion") String descripcion);
}