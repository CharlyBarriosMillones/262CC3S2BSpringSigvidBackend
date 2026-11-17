package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {
    List<MovimientoCaja> findByCaja_IdCajaOrderByFechaDesc(Long idCaja);
}