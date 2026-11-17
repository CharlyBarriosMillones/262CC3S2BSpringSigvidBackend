package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);

    Optional<Rol> findByNombreIgnoreCase(String nombre);

    @Query("SELECT r FROM Rol r WHERE (:nombre IS NULL OR LOWER(r.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) ORDER BY r.idRol ASC")
    List<Rol> findByFilter(@Param("nombre") String nombre);
}