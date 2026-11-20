package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findAllByOrderByProducto_CodigoProductoAscAlmacen_NombreAsc();
    Optional<Stock> findByProducto_IdProductoAndAlmacen_IdAlmacen(Long idProducto, Long idAlmacen);
    List<Stock> findByProducto_IdProducto(Long idProducto);
}
