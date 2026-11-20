package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.AuthResponseDTO;
import dsw.sigvidbackend.dto.LoginRequestDTO;
import dsw.sigvidbackend.dto.ModuloResponse;
import dsw.sigvidbackend.dto.RegisterRequestDTO;
import dsw.sigvidbackend.model.*;
import dsw.sigvidbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final CredencialRepository credencialRepository;
    private final RolRepository rolRepository;
    private final ModuloRepository moduloRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final VendedorRepository vendedorRepository;
    private final CajeroRepository cajeroRepository;
    private final TrabajadorAlmacenRepository trabajadorAlmacenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthResponseDTO login(LoginRequestDTO request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new UsernameNotFoundException("No existe una cuenta registrada con el correo: " + request.getEmail().trim()));

        Credencial credencial = credencialRepository.findByUsuario(usuario)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales no encontradas para este usuario"));

        if (!passwordEncoder.matches(request.getPassword(), credencial.getPasswordHash())) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }

        credencial.setUltimoAcceso(LocalDateTime.now());
        credencialRepository.save(credencial);

        List<Modulo> modulos = Collections.emptyList();
        if (usuario.getRol() != null) {
            modulos = moduloRepository.findByRolId(usuario.getRol().getIdRol());
        }

        String token = jwtUtil.generateToken(usuario, modulos);

        return AuthResponseDTO.builder()
                .token(token)
                .idUsuario(usuario.getIdUsuario())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .rol(usuario.getRol() != null ? usuario.getRol().getNombre() : "Cliente")
                .modulos(ModuloResponse.fromEntities(modulos))
                .build();
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }
        if (usuarioRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }
        if (request.getDni() != null && !request.getDni().isBlank() && usuarioRepository.existsByDni(request.getDni().trim())) {
            throw new IllegalArgumentException("El DNI ingresado ya se encuentra registrado");
        }

        // el registro publico siempre crea un Cliente; el personal (vendedor, cajero, etc.)
        // lo crea el Administrador desde /api/usuarios (evita escalada de privilegios)
        String rolBuscado = "Cliente";

        Rol rol = rolRepository.findByNombreIgnoreCase(rolBuscado)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el rol '" + rolBuscado + "' en la base de datos"));

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(request.getNombre().trim());
        nuevoUsuario.setApellido(request.getApellido().trim());
        nuevoUsuario.setDni(request.getDni() != null ? request.getDni().trim() : null);
        nuevoUsuario.setEmail(request.getEmail().trim());
        nuevoUsuario.setTelefono(request.getTelefono());
        nuevoUsuario.setTipoUsuario(rol.getNombre().toUpperCase());
        nuevoUsuario.setRol(rol);
        nuevoUsuario.setIdSucursal(1L);
        nuevoUsuario.setCreatedAt(LocalDateTime.now());
        nuevoUsuario.setUpdatedAt(LocalDateTime.now());

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        String hashGenerado = passwordEncoder.encode(request.getPassword());
        String salExtraida = hashGenerado.substring(0, 29);

        Credencial nuevaCredencial = new Credencial();
        nuevaCredencial.setUsuario(usuarioGuardado);
        nuevaCredencial.setPasswordHash(hashGenerado);
        nuevaCredencial.setSal(salExtraida);
        nuevaCredencial.setUltimoAcceso(LocalDateTime.now());
        credencialRepository.save(nuevaCredencial);

        guardarEnTablaEspecializada(usuarioGuardado.getIdUsuario(), rol.getNombre(), request);

        List<Modulo> modulos = moduloRepository.findByRolId(rol.getIdRol());
        String token = jwtUtil.generateToken(usuarioGuardado, modulos);

        return AuthResponseDTO.builder()
                .token(token)
                .idUsuario(usuarioGuardado.getIdUsuario())
                .email(usuarioGuardado.getEmail())
                .nombre(usuarioGuardado.getNombre())
                .apellido(usuarioGuardado.getApellido())
                .rol(rol.getNombre())
                .modulos(ModuloResponse.fromEntities(modulos))
                .build();
    }

    @Transactional
    public void recuperarPassword(String email, String dni, String nuevaPassword) {
        if (email == null || dni == null || nuevaPassword == null || nuevaPassword.length() < 6) {
            throw new IllegalArgumentException("Datos incompletos o contraseña menor a 6 caracteres");
        }

        Usuario usuario = usuarioRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("No existe una cuenta con ese correo electrónico"));

        if (usuario.getDni() == null || !usuario.getDni().equals(dni.trim())) {
            throw new IllegalArgumentException("El DNI ingresado no coincide con el titular del correo");
        }

        Credencial credencial = credencialRepository.findByUsuario(usuario)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales no encontradas para este usuario"));

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
                        (request.getRuc() != null && !request.getRuc().isBlank()) ? request.getRuc().trim() : null,
                        request.getRazonSocial(),
                        request.getDireccion()
                );
                clienteRepository.save(cliente);
            }
            case "administrador" -> {
                String nivel = (request.getNivel() != null && !request.getNivel().isBlank())
                        ? request.getNivel()
                        : "General";
                administradorRepository.save(new Administrador(idUsuario, nivel));
            }
            case "vendedor" -> {
                String area = (request.getArea() != null && !request.getArea().isBlank())
                        ? request.getArea()
                        : "Ventas Generales";
                vendedorRepository.save(new Vendedor(idUsuario, area));
            }
            case "cajero" -> {
                String caja = (request.getCajaAsignada() != null && !request.getCajaAsignada().isBlank())
                        ? request.getCajaAsignada()
                        : "Caja 1";
                cajeroRepository.save(new Cajero(idUsuario, caja));
            }
            case "almacenero" -> {
                String sede = (request.getUbicacionSede() != null && !request.getUbicacionSede().isBlank())
                        ? request.getUbicacionSede()
                        : "Almacén Central";
                trabajadorAlmacenRepository.save(new TrabajadorAlmacen(idUsuario, sede));
            }
        }
    }
}