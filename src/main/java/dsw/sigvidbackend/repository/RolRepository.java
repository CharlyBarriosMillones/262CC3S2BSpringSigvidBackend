package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    
    // se usa al registrar nuevos usuarios
    Optional<Rol> findByNombre(String nombre);
}