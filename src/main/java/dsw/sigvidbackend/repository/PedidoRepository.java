package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByCodigoPedidoIgnoreCase(String codigoPedido);

    List<Pedido> findAllByOrderByFechaSolicitudDesc();

    List<Pedido> findByCliente_IdUsuarioOrderByFechaSolicitudDesc(Long idCliente);

    List<Pedido> findByCliente_EmailOrderByFechaSolicitudDesc(String email);

    List<Pedido> findByEstado_NombreIgnoreCaseOrderByFechaSolicitudDesc(String nombreEstado);

    @Query("SELECT p FROM Pedido p WHERE " +
           "LOWER(p.codigoPedido) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR p.cliente.dni LIKE CONCAT('%', :termino, '%') " +
           "ORDER BY p.fechaSolicitud DESC")
    List<Pedido> buscarPorCodigoODniCliente(@Param("termino") String termino);
}