package dsw.sigvidbackend.controller;

import dsw.sigvidbackend.dto.ModuloRequest;
import dsw.sigvidbackend.dto.ModuloResponse;
import dsw.sigvidbackend.service.ModuloService;
import dsw.sigvidbackend.util.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/modulos")
@RequiredArgsConstructor
public class ModuloController {

    private static final Logger logger = LoggerFactory.getLogger(ModuloController.class);
    private final ModuloService moduloService;

    @GetMapping("/rol/{idRol}")
    public ResponseEntity<List<ModuloResponse>> listarPorRol(@PathVariable Long idRol) {
        return ResponseEntity.ok(moduloService.listByRol(idRol));
    }

    @PostMapping("/list")
    public ResponseEntity<?> list(@RequestBody(required = false) ModuloRequest request) {
        return ResponseEntity.ok(moduloService.list(request));
    }

    @PostMapping("/find")
    public ResponseEntity<?> find(@RequestBody Optional<ModuloRequest> request) {
        logger.info(">find {}", request);
        if (request.isEmpty() || request.get().getIdMod() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder().status(400).error("Bad Request").message("El ID del Módulo es obligatorio").build());
        }
        ModuloResponse moduloResponse = moduloService.find(request.get().getIdMod());
        if (moduloResponse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder().status(404).error("Not Found").message("Módulo no encontrado").build());
        }
        return ResponseEntity.ok(moduloResponse);
    }
}