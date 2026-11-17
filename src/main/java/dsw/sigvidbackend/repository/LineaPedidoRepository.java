package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.LineaPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LineaPedidoRepository extends JpaRepository<LineaPedido, Long> {
    List<LineaPedido> findByPedido_IdPedido(Long idPedido);
}