package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.DetalleGuia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetalleGuiaRepository extends JpaRepository<DetalleGuia, Long> {
    List<DetalleGuia> findByGuia_IdGuiaOrderByIdDetalleAsc(Long idGuia);
    // cantidades reservadas en guias de un estado (Preliminar) para un producto y almacen
    List<DetalleGuia> findByGuia_EstadoAndGuia_Almacen_IdAlmacenAndProducto_IdProducto(String estado, Long idAlmacen, Long idProducto);
}
