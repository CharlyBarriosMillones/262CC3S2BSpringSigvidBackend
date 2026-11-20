package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Ubigeo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UbigeoRepository extends JpaRepository<Ubigeo, String> {
    List<Ubigeo> findAllByOrderByDepartamentoAscProvinciaAscDistritoAsc();
}
