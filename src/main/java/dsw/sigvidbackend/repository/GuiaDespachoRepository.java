package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.GuiaDespacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuiaDespachoRepository extends JpaRepository<GuiaDespacho, Long> {
    Optional<GuiaDespacho> findByPedido_IdPedido(Long idPedido);
    boolean existsByPedido_IdPedido(Long idPedido);
    boolean existsByCodigoGuia(String codigoGuia);
    List<GuiaDespacho> findByEstadoOrderByFechaGeneracionAsc(String estado);
    List<GuiaDespacho> findByPedido_Cliente_EmailAndPedido_Estado_NombreIgnoreCaseOrderByFechaGeneracionDesc(String email, String estadoPedido);
}
