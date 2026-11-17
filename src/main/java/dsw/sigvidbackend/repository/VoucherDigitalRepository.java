package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.VoucherDigital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherDigitalRepository extends JpaRepository<VoucherDigital, Long> {
    Optional<VoucherDigital> findByPago_IdPago(Long idPago);
}