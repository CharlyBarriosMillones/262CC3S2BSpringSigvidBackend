package dsw.sigvidbackend.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dsw.sigvidbackend.dto.AuthResponseDTO;
import dsw.sigvidbackend.dto.LoginRequestDTO;
import dsw.sigvidbackend.dto.RegisterRequestDTO;
import dsw.sigvidbackend.model.Credencial;
import dsw.sigvidbackend.model.Rol;
import dsw.sigvidbackend.model.Usuario;
import dsw.sigvidbackend.repository.CredencialRepository;
import dsw.sigvidbackend.repository.RolRepository;
import dsw.sigvidbackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final CredencialRepository credencialRepository;
    private final RolRepository rolRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

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
            throw new RuntimeException("Error: Contrasenia incorrecta");
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

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // verificamos que el correo no este registrado
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Error: El correo electrónico ya está registrado");
        }

        // buuscamos el rol en la BD (si no se especifica sera "Cliente" por defecto)
        String rolBuscado = (request.getNombreRol() != null && !request.getNombreRol().isBlank())
                ? request.getNombreRol()
                : "Cliente";

        Rol rol = rolRepository.findByNombre(rolBuscado)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró el rol '" + rolBuscado + "' en la base de datos"));

        // crear y guardar el usuario en la tabla 'usuarios'
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setApellido(request.getApellido());
        nuevoUsuario.setDni(request.getDni());
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setTelefono(request.getTelefono());
        nuevoUsuario.setTipoUsuario(
                (request.getTipoUsuario() != null && !request.getTipoUsuario().isBlank())
                        ? request.getTipoUsuario()
                        : "CLIENTE"
        );
        nuevoUsuario.setRol(rol);
        nuevoUsuario.setCreatedAt(LocalDateTime.now());
        nuevoUsuario.setUpdatedAt(LocalDateTime.now());

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        // encriptamos la contraseña y guardamos en la tabla 'credenciales'
        String hashGenerado = passwordEncoder.encode(request.getPassword());
        String salExtraida = hashGenerado.substring(0, 29); // sal interna de BCrypt

        Credencial nuevaCredencial = new Credencial();
        nuevaCredencial.setUsuario(usuarioGuardado);
        nuevaCredencial.setPasswordHash(hashGenerado);
        nuevaCredencial.setSal(salExtraida);
        nuevaCredencial.setUltimoAcceso(LocalDateTime.now());

        credencialRepository.save(nuevaCredencial);

        // generamos el token JWT y se retorna respuesta
        String token = jwtUtil.generateToken(usuarioGuardado.getEmail());

        return new AuthResponseDTO(
                token,
                usuarioGuardado.getEmail(),
                usuarioGuardado.getNombre(),
                usuarioGuardado.getApellido(),
                rol.getNombre()
        );
    }
}