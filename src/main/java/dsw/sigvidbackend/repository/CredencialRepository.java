package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Credencial;
import dsw.sigvidbackend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CredencialRepository extends JpaRepository<Credencial, Long> {
    
    // buscamos las credenciales de un usuario
    Optional<Credencial> findByUsuario(Usuario usuario);
    
}
