package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
}