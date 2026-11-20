package dsw.sigvidbackend.controller;

import jakarta.validation.Valid;

import dsw.sigvidbackend.dto.RolRequest;
import dsw.sigvidbackend.dto.RolResponse;
import dsw.sigvidbackend.service.RolService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private static final Logger logger = LoggerFactory.getLogger(RolController.class);
    private final RolService rolService;

    @GetMapping
    public ResponseEntity<List<RolResponse>> listarTodos() {
        return ResponseEntity.ok(rolService.listarRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(rolService.obtenerPorId(id));
    }

    @PostMapping("/save")
    public ResponseEntity<RolResponse> guardar(@Valid @RequestBody RolRequest request) {
        logger.info(">save rol: {}", request);
        return ResponseEntity.ok(rolService.guardar(request));
    }
}