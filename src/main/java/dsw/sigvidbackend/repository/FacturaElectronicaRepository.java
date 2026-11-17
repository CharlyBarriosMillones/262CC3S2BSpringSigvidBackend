package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.FacturaElectronica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacturaElectronicaRepository extends JpaRepository<FacturaElectronica, Long> {
    Optional<FacturaElectronica> findByPedido_IdPedido(Long idPedido);
}