package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Cajero;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CajeroRepository extends JpaRepository<Cajero, Long> {
}