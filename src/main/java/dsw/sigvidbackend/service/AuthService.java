package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.AuthResponseDTO;
import dsw.sigvidbackend.dto.LoginRequestDTO;
import dsw.sigvidbackend.model.Credencial;
import dsw.sigvidbackend.model.Usuario;
import dsw.sigvidbackend.repository.CredencialRepository;
import dsw.sigvidbackend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CredencialRepository credencialRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponseDTO login(LoginRequestDTO request) {
        
        // verificamos si el usuario existe por su email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado"));

        // buscamos las credenciales de ese usuario
        Credencial credencial = credencialRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Error: Credenciales no encontradas para este usuario"));

        // validamos contraseña
        // comparamos con el texto plano que llega de angular con el hash de la BD
        if (!passwordEncoder.matches(request.getPassword(), credencial.getPasswordHash())) {
            throw new RuntimeException("Error: Contraseña incorrecta");
        }

        // atualizamos la fecha del ultimo acceso
        credencial.setUltimoAcceso(LocalDateTime.now());
        credencialRepository.save(credencial);

        // si la contraseña es correcta generamos el token jwt
        String token = jwtUtil.generateToken(usuario.getEmail());

        // hacemos y devolvemos la respuesta para angular
        return new AuthResponseDTO(
                token,
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol().getNombre()
        );
    }
}