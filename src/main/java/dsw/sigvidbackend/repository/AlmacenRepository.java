package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlmacenRepository extends JpaRepository<Almacen, Long> {
    List<Almacen> findByActivoTrueOrderByNombreAsc();
}
