package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}