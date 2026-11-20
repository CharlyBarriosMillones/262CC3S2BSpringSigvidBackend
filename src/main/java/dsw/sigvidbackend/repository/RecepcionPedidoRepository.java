package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.RecepcionPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecepcionPedidoRepository extends JpaRepository<RecepcionPedido, Long> {
    Optional<RecepcionPedido> findByPedido_IdPedido(Long idPedido);
    boolean existsByPedido_IdPedido(Long idPedido);
}
