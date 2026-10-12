package dsw.sigvidbackend.repository;

import dsw.sigvidbackend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // spring boot traduce esto a: 
    // SELECT * FROM usuarios WHERE email = ?
    Optional<Usuario> findByEmail(String email);
    
}