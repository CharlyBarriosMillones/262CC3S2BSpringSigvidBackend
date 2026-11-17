package dsw.sigvidbackend.controller;

import dsw.sigvidbackend.dto.PerfilUpdateRequest;
import dsw.sigvidbackend.dto.UsuarioRequest;
import dsw.sigvidbackend.dto.UsuarioResponse;
import dsw.sigvidbackend.service.UsuarioService;
import dsw.sigvidbackend.util.ObjectResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> obtenerPerfil(@RequestParam String email) {
        return ResponseEntity.ok(usuarioService.obtenerPerfilPorEmail(email));
    }

    @PutMapping("/perfil")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(@RequestBody PerfilUpdateRequest datos) {
        logger.info(">actualizarPerfil: {}", datos.getEmail());
        return ResponseEntity.ok(usuarioService.actualizarPerfilPorEmail(datos));
    }

    @GetMapping("/buscar-documento/{documento}")
    public ResponseEntity<UsuarioResponse> buscarPorDocumento(@PathVariable String documento) {
        return ResponseEntity.ok(usuarioService.buscarPorDocumento(documento));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@RequestBody UsuarioRequest request) {
        logger.info(">crear usuario: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearUsuario(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @RequestBody UsuarioRequest request) {
        logger.info(">actualizar usuario id={}", id);
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ObjectResponse<Void>> eliminar(@PathVariable Long id) {
        logger.info(">eliminar usuario id={}", id);
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok(
                ObjectResponse.<Void>builder()
                        .success(true)
                        .message("Usuario eliminado correctamente")
                        .build()
        );
    }
}