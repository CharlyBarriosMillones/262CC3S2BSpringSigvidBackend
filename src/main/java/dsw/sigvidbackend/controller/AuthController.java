package dsw.sigvidbackend.controller;

import jakarta.validation.Valid;

import dsw.sigvidbackend.dto.AuthResponseDTO;
import dsw.sigvidbackend.dto.LoginRequestDTO;
import dsw.sigvidbackend.dto.RecuperarPasswordRequestDTO;
import dsw.sigvidbackend.dto.RegisterRequestDTO;
import dsw.sigvidbackend.service.AuthService;
import dsw.sigvidbackend.util.ObjectResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        logger.info(">login: {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        logger.info(">register: {}", request.getEmail());
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<ObjectResponse<Void>> recuperarPassword(@Valid @RequestBody RecuperarPasswordRequestDTO request) {
        logger.info(">recuperarPassword: {}", request.getEmail());
        authService.recuperarPassword(
                request.getEmail(),
                request.getDni(),
                request.getNuevaPassword()
        );
        return ResponseEntity.ok(
                ObjectResponse.<Void>builder()
                        .success(true)
                        .message("Contraseña actualizada correctamente en la base de datos")
                        .build()
        );
    }
}