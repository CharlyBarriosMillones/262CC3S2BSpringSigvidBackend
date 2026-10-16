package dsw.sigvidbackend.controller;

import dsw.sigvidbackend.dto.UsuarioRequestDTO;
import dsw.sigvidbackend.dto.UsuarioResponseDTO;
import dsw.sigvidbackend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    // obtener datos completos de perfil
    @GetMapping("/perfil")
    public ResponseEntity<Map<String, Object>> obtenerPerfil(@RequestParam String email) {
        return ResponseEntity.ok(usuarioService.obtenerPerfilPorEmail(email));
    }

    // actualizar nombre, apellido, telefono, foto y notificaciones
    @PutMapping("/perfil")
    public ResponseEntity<Map<String, Object>> actualizarPerfil(@RequestBody Map<String, Object> datos) {
        return ResponseEntity.ok(usuarioService.actualizarPerfilPorEmail(datos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crear(@RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearUsuario(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}