package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // busqueda de cliente por RUC (HU04)
    Optional<Cliente> findByRuc(String ruc);
}