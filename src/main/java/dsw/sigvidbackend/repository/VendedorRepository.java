package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendedorRepository extends JpaRepository<Vendedor, Long> {
}