package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.RolModulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolModuloRepository extends JpaRepository<RolModulo, RolModulo.RolModuloId> {
    List<RolModulo> findByIdRol(Long idRol);
}