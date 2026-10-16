package dsw.sigvidbackend.controller;

import dsw.sigvidbackend.dto.AuthResponseDTO;
import dsw.sigvidbackend.dto.LoginRequestDTO;
import dsw.sigvidbackend.dto.RegisterRequestDTO;
import dsw.sigvidbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // recuperar contraseña validando email y DNI
    @PostMapping("/recuperar-password")
    public ResponseEntity<Map<String, String>> recuperarPassword(@RequestBody Map<String, String> request) {
        authService.recuperarPassword(
                request.get("email"),
                request.get("dni"),
                request.get("nuevaPassword")
        );
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente en la base de datos"));
    }
}