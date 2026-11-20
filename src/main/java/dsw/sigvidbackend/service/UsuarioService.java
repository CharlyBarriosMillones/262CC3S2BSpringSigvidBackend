package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.PerfilUpdateRequest;
import dsw.sigvidbackend.dto.UsuarioRequest;
import dsw.sigvidbackend.dto.UsuarioResponse;
import dsw.sigvidbackend.model.*;
import dsw.sigvidbackend.repository.*;
import dsw.sigvidbackend.util.Patrones;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

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

    public List<UsuarioResponse> listarUsuarios() {
        return UsuarioResponse.fromEntities(usuarioRepository.findAllByOrderByIdUsuarioDesc());
    }

    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));
        return UsuarioResponse.fromEntity(usuario);
    }

    // busca al cliente por DNI (8 digitos) o por RUC (11 digitos, tabla clientes)
    public UsuarioResponse buscarPorDocumento(String documento) {
        String doc = documento == null ? "" : documento.trim();
        if (doc.matches(Patrones.DNI)) {
            Usuario usuario = usuarioRepository.findByDni(doc)
                    .orElseThrow(() -> new NoSuchElementException("No se encontró cliente con DNI: " + doc));
            validarEsCliente(usuario);
            return UsuarioResponse.fromEntity(usuario);
        }
        if (doc.matches(Patrones.RUC)) {
            Cliente cliente = clienteRepository.findByRuc(doc)
                    .orElseThrow(() -> new NoSuchElementException("No se encontró cliente con RUC: " + doc));
            Usuario usuario = usuarioRepository.findById(cliente.getIdUsuario())
                    .orElseThrow(() -> new NoSuchElementException("No se encontró el usuario del RUC: " + doc));
            validarEsCliente(usuario);
            return UsuarioResponse.fromEntity(usuario);
        }
        throw new IllegalArgumentException("El documento debe ser un DNI de 8 dígitos o un RUC de 11 dígitos");
    }

    private void validarEsCliente(Usuario usuario) {
        String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombre() : "";
        if (!"Cliente".equalsIgnoreCase(rolNombre)) {
            throw new NoSuchElementException("El documento ingresado no corresponde a un cliente registrado.");
        }
    }

    public UsuarioResponse obtenerPerfilPorEmail(String email) {
        Usuario u = usuarioRepository.findByEmail(email.trim())
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con email: " + email));
        return UsuarioResponse.fromEntity(u);
    }

    @Transactional
    public UsuarioResponse actualizarPerfilPorEmail(PerfilUpdateRequest datos) {
        if (datos.getEmail() == null || datos.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo del usuario es obligatorio para actualizar el perfil");
        }
        Usuario usuario = usuarioRepository.findByEmail(datos.getEmail().trim())
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con email: " + datos.getEmail()));

        if (datos.getNombre() != null && !datos.getNombre().isBlank()) {
            usuario.setNombre(datos.getNombre().trim());
        }
        if (datos.getApellido() != null && !datos.getApellido().isBlank()) {
            usuario.setApellido(datos.getApellido().trim());
        }
        if (datos.getTelefono() != null) {
            usuario.setTelefono(datos.getTelefono().trim());
        }
        usuario.setFotoPerfil(datos.getFotoPerfil());

        if (datos.getPreferenciaCategoria() != null) {
            usuario.setPreferenciaCategoria(datos.getPreferenciaCategoria());
        }
        if (datos.getPreferenciaMaridaje() != null) {
            usuario.setPreferenciaMaridaje(datos.getPreferenciaMaridaje());
        }
        if (datos.getNotifEstadoPedidos() != null) {
            usuario.setNotifPedidos(datos.getNotifEstadoPedidos());
        }
        if (datos.getNotifPromocionesEmail() != null) {
            usuario.setNotifPromociones(datos.getNotifPromocionesEmail());
        }
        if (datos.getNotifNuevasCosechas() != null) {
            usuario.setNotifCosechas(datos.getNotifNuevasCosechas());
        }

        usuario.setUpdatedAt(LocalDateTime.now());
        Usuario guardado = usuarioRepository.saveAndFlush(usuario);
        return UsuarioResponse.fromEntity(guardado);
    }

    @Transactional
    public UsuarioResponse crearUsuario(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }
        if (request.getDni() != null && usuarioRepository.existsByDni(request.getDni().trim())) {
            throw new IllegalArgumentException("El DNI ya está registrado");
        }

        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new NoSuchElementException("Rol no encontrado con ID: " + request.getIdRol()));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setDni(request.getDni().trim());
        usuario.setEmail(request.getEmail().trim());
        usuario.setTelefono(request.getTelefono());
        usuario.setTipoUsuario(
                (request.getTipoUsuario() != null && !request.getTipoUsuario().isBlank())
                        ? request.getTipoUsuario()
                        : rol.getNombre().toUpperCase()
        );
        usuario.setRol(rol);
        usuario.setIdSucursal(request.getIdSucursal() != null ? request.getIdSucursal() : 1L);
        usuario.setCreatedAt(LocalDateTime.now());
        usuario.setUpdatedAt(LocalDateTime.now());

        Usuario guardado = usuarioRepository.saveAndFlush(usuario);

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : request.getDni();

        String hash = passwordEncoder.encode(rawPassword);
        Credencial credencial = new Credencial();
        credencial.setUsuario(guardado);
        credencial.setPasswordHash(hash);
        credencial.setSal(hash.substring(0, 29));
        credencial.setUltimoAcceso(LocalDateTime.now());
        credencialRepository.save(credencial);

        registrarEspecializacionPorDefecto(guardado.getIdUsuario(), rol.getNombre());

        return UsuarioResponse.fromEntity(guardado);
    }

    @Transactional
    public UsuarioResponse actualizarUsuario(Long id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));

        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new NoSuchElementException("Rol no encontrado con ID: " + request.getIdRol()));

        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setDni(request.getDni().trim());
        usuario.setEmail(request.getEmail().trim());
        usuario.setTelefono(request.getTelefono());
        if (request.getTipoUsuario() != null && !request.getTipoUsuario().isBlank()) {
            usuario.setTipoUsuario(request.getTipoUsuario());
        }
        usuario.setRol(rol);
        if (request.getIdSucursal() != null) {
            usuario.setIdSucursal(request.getIdSucursal());
        }
        usuario.setUpdatedAt(LocalDateTime.now());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            credencialRepository.findByUsuario(usuario).ifPresent(cred -> {
                String nuevoHash = passwordEncoder.encode(request.getPassword());
                cred.setPasswordHash(nuevoHash);
                cred.setSal(nuevoHash.substring(0, 29));
                credencialRepository.save(cred);
            });
        }

        Usuario actualizado = usuarioRepository.saveAndFlush(usuario);
        return UsuarioResponse.fromEntity(actualizado);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));

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
}