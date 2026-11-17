package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findAllByOrderByFechaDesc();
    List<Pago> findByPedido_IdPedido(Long idPedido);
}