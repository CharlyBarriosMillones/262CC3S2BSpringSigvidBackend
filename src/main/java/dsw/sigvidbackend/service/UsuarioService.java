package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.UsuarioRequestDTO;
import dsw.sigvidbackend.dto.UsuarioResponseDTO;
import dsw.sigvidbackend.model.*;
import dsw.sigvidbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final CredencialRepository credencialRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final VendedorRepository vendedorRepository;
    private final CajeroRepository cajeroRepository;
    private final TrabajadorAlmacenRepository trabajadorAlmacenRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponseDTO> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado con ID: " + id));
        return mapToResponse(usuario);
    }

    // consultar perfil del usuario logueado
    public Map<String, Object> obtenerPerfilPorEmail(String email) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado con email: " + email));
        return mapPerfilToMap(u);
    }

    // guardar cambios de perfil, foto, preferencias y notificaciones
    @Transactional
    public Map<String, Object> actualizarPerfilPorEmail(Map<String, Object> datos) {
        String email = (String) datos.get("email");
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado con email: " + email));

        if (datos.get("nombre") != null) {
            usuario.setNombre(datos.get("nombre").toString().trim());
        }
        if (datos.get("apellido") != null) {
            usuario.setApellido(datos.get("apellido").toString().trim());
        }
        if (datos.containsKey("telefono")) {
            usuario.setTelefono(datos.get("telefono") != null ? datos.get("telefono").toString().trim() : null);
        }
        if (datos.containsKey("fotoPerfil")) {
            usuario.setFotoPerfil(datos.get("fotoPerfil") != null ? datos.get("fotoPerfil").toString() : null);
        }
        if (datos.get("preferenciaCategoria") != null) {
            usuario.setPreferenciaCategoria(datos.get("preferenciaCategoria").toString());
        }
        if (datos.get("preferenciaMaridaje") != null) {
            usuario.setPreferenciaMaridaje(datos.get("preferenciaMaridaje").toString());
        }
        if (datos.get("notifEstadoPedidos") != null) {
            usuario.setNotifPedidos(Boolean.valueOf(datos.get("notifEstadoPedidos").toString()));
        }
        if (datos.get("notifPromocionesEmail") != null) {
            usuario.setNotifPromociones(Boolean.valueOf(datos.get("notifPromocionesEmail").toString()));
        }
        if (datos.get("notifNuevasCosechas") != null) {
            usuario.setNotifCosechas(Boolean.valueOf(datos.get("notifNuevasCosechas").toString()));
        }

        usuario.setUpdatedAt(LocalDateTime.now());
        Usuario guardado = usuarioRepository.save(usuario);
        return mapPerfilToMap(guardado);
    }

    private Map<String, Object> mapPerfilToMap(Usuario u) {
        Map<String, Object> map = new HashMap<>();
        map.put("idUsuario", u.getIdUsuario());
        map.put("nombre", u.getNombre());
        map.put("apellido", u.getApellido());
        map.put("dni", u.getDni());
        map.put("email", u.getEmail());
        map.put("telefono", u.getTelefono());
        map.put("rol", u.getRol().getNombre());
        map.put("fotoPerfil", u.getFotoPerfil());
        map.put("preferenciaCategoria", u.getPreferenciaCategoria() != null ? u.getPreferenciaCategoria() : "Vinos Tintos Gran Reserva");
        map.put("preferenciaMaridaje", u.getPreferenciaMaridaje() != null ? u.getPreferenciaMaridaje() : "Carnes rojas y quesos maduros");
        map.put("notifEstadoPedidos", u.getNotifPedidos() != null ? u.getNotifPedidos() : true);
        map.put("notifPromocionesEmail", u.getNotifPromociones() != null ? u.getNotifPromociones() : true);
        map.put("notifNuevasCosechas", u.getNotifCosechas() != null ? u.getNotifCosechas() : false);
        return map;
    }

    @Transactional
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Error: El correo electrónico ya está registrado");
        }

        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado con ID: " + request.getIdRol()));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setDni(request.getDni());
        usuario.setEmail(request.getEmail());
        usuario.setTelefono(request.getTelefono());
        usuario.setTipoUsuario(
                (request.getTipoUsuario() != null && !request.getTipoUsuario().isBlank())
                        ? request.getTipoUsuario()
                        : rol.getNombre().toUpperCase()
        );
        usuario.setRol(rol);
        usuario.setCreatedAt(LocalDateTime.now());
        usuario.setUpdatedAt(LocalDateTime.now());

        Usuario guardado = usuarioRepository.save(usuario);

        // se crea credencial por defecto o con la contraseña enviada
        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : request.getDni(); // si no se envia clave, se usa DNI como clave inicial

        String hash = passwordEncoder.encode(rawPassword);
        Credencial credencial = new Credencial();
        credencial.setUsuario(guardado);
        credencial.setPasswordHash(hash);
        credencial.setSal(hash.substring(0, 29));
        credencial.setUltimoAcceso(LocalDateTime.now());
        credencialRepository.save(credencial);

        // registrar en su tabla segun el rol
        registrarEspecializacionPorDefecto(guardado.getIdUsuario(), rol.getNombre());

        return mapToResponse(guardado);
    }

    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado con ID: " + id));

        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado con ID: " + request.getIdRol()));

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setDni(request.getDni());
        usuario.setEmail(request.getEmail());
        usuario.setTelefono(request.getTelefono());
        if (request.getTipoUsuario() != null && !request.getTipoUsuario().isBlank()) {
            usuario.setTipoUsuario(request.getTipoUsuario());
        }
        usuario.setRol(rol);
        usuario.setUpdatedAt(LocalDateTime.now());

        // si envia una nueva contraseña al actualizar, se actualiza en credenciales
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            credencialRepository.findByUsuario(usuario).ifPresent(cred -> {
                String nuevoHash = passwordEncoder.encode(request.getPassword());
                cred.setPasswordHash(nuevoHash);
                cred.setSal(nuevoHash.substring(0, 29));
                credencialRepository.save(cred);
            });
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return mapToResponse(actualizado);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado con ID: " + id));

        // limpiar registros en tablas antes de eliminar el usuario base
        if (clienteRepository.existsById(id)) clienteRepository.deleteById(id);
        if (administradorRepository.existsById(id)) administradorRepository.deleteById(id);
        if (vendedorRepository.existsById(id)) vendedorRepository.deleteById(id);
        if (cajeroRepository.existsById(id)) cajeroRepository.deleteById(id);
        if (trabajadorAlmacenRepository.existsById(id)) trabajadorAlmacenRepository.deleteById(id);

        credencialRepository.findByUsuario(usuario).ifPresent(credencialRepository::delete);
        usuarioRepository.delete(usuario);
    }

    private void registrarEspecializacionPorDefecto(Long idUsuario, String nombreRol) {
        switch (nombreRol.toLowerCase()) {
            case "cliente" -> clienteRepository.save(new Cliente(idUsuario, null, null, null));
            case "administrador" -> administradorRepository.save(new Administrador(idUsuario, "General"));
            case "vendedor" -> vendedorRepository.save(new Vendedor(idUsuario, "Ventas Generales"));
            case "cajero" -> cajeroRepository.save(new Cajero(idUsuario, "Caja 1"));
            case "almacenero" -> trabajadorAlmacenRepository.save(new TrabajadorAlmacen(idUsuario, "Almacén Central"));
        }
    }

    private UsuarioResponseDTO mapToResponse(Usuario u) {
        return new UsuarioResponseDTO(
                u.getIdUsuario(),
                u.getNombre(),
                u.getApellido(),
                u.getDni(),
                u.getEmail(),
                u.getTelefono(),
                u.getTipoUsuario(),
                u.getRol().getIdRol(),
                u.getRol().getNombre(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}