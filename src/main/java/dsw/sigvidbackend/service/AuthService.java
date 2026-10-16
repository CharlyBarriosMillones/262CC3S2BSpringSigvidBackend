package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.AuthResponseDTO;
import dsw.sigvidbackend.dto.LoginRequestDTO;
import dsw.sigvidbackend.dto.RegisterRequestDTO;
import dsw.sigvidbackend.model.*;
import dsw.sigvidbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final CredencialRepository credencialRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final VendedorRepository vendedorRepository;
    private final CajeroRepository cajeroRepository;
    private final TrabajadorAlmacenRepository trabajadorAlmacenRepository;
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
                        : rol.getNombre().toUpperCase()
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

        // guardar en la tabla segun el rol
        guardarEnTablaEspecializada(usuarioGuardado.getIdUsuario(), rol.getNombre(), request);

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

    // reesstablecer contraseña en la tabla 'credenciales' verificando email y DNI
    @Transactional
    public void recuperarPassword(String email, String dni, String nuevaPassword) {
        if (email == null || dni == null || nuevaPassword == null || nuevaPassword.length() < 6) {
            throw new RuntimeException("Error: Datos incompletos o contraseña menor a 6 caracteres");
        }

        Usuario usuario = usuarioRepository.findByEmail(email.trim())
                .orElseThrow(() -> new RuntimeException("Error: No existe una cuenta con ese correo electrónico"));

        if (!usuario.getDni().equals(dni.trim())) {
            throw new RuntimeException("Error: El DNI ingresado no coincide con el titular del correo");
        }

        Credencial credencial = credencialRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Error: Credenciales no encontradas para este usuario"));

        String nuevoHash = passwordEncoder.encode(nuevaPassword);
        credencial.setPasswordHash(nuevoHash);
        credencial.setSal(nuevoHash.substring(0, 29));
        credencial.setUltimoAcceso(LocalDateTime.now());
        credencialRepository.save(credencial);
    }

    private void guardarEnTablaEspecializada(Long idUsuario, String nombreRol, RegisterRequestDTO request) {
        switch (nombreRol.toLowerCase()) {
            case "cliente" -> {
                Cliente cliente = new Cliente(
                        idUsuario,
                        request.getRuc(),
                        request.getRazonSocial(),
                        request.getDireccion()
                );
                clienteRepository.save(cliente);
            }
            case "administrador" -> {
                String nivel = (request.getNivel() != null && !request.getNivel().isBlank())
                        ? request.getNivel()
                        : "General";
                Administrador admin = new Administrador(idUsuario, nivel);
                administradorRepository.save(admin);
            }
            case "vendedor" -> {
                String area = (request.getArea() != null && !request.getArea().isBlank())
                        ? request.getArea()
                        : "Ventas Generales";
                Vendedor vendedor = new Vendedor(idUsuario, area);
                vendedorRepository.save(vendedor);
            }
            case "cajero" -> {
                String caja = (request.getCajaAsignada() != null && !request.getCajaAsignada().isBlank())
                        ? request.getCajaAsignada()
                        : "Caja 1";
                Cajero cajero = new Cajero(idUsuario, caja);
                cajeroRepository.save(cajero);
            }
            case "almacenero" -> {
                String sede = (request.getUbicacionSede() != null && !request.getUbicacionSede().isBlank())
                        ? request.getUbicacionSede()
                        : "Almacén Central";
                TrabajadorAlmacen almacenero = new TrabajadorAlmacen(idUsuario, sede);
                trabajadorAlmacenRepository.save(almacenero);
            }
        }
    }
}