package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigoProducto(String codigoProducto);

    boolean existsByCodigoProducto(String codigoProducto);

    @Query("SELECT p FROM Producto p WHERE (p.activo IS NULL OR p.activo = true) " +
           "AND (:idCategoria IS NULL OR p.categoria.idCategoria = :idCategoria) " +
           "AND (:idMarca IS NULL OR p.marca.idMarca = :idMarca) " +
           "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
           "OR LOWER(p.codigoProducto) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
           "ORDER BY p.idProducto ASC")
    List<Producto> buscarConFiltros(
            @Param("idCategoria") Long idCategoria,
            @Param("idMarca") Long idMarca,
            @Param("nombre") String nombre
    );
}