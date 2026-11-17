package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigoProducto(String codigoProducto);

    boolean existsByCodigoProducto(String codigoProducto);

    List<Producto> findAllByOrderByIdProductoAsc();
}