package dsw.sigvidbackend.controller;

import jakarta.validation.Valid;

import dsw.sigvidbackend.dto.SucursalRequest;
import dsw.sigvidbackend.dto.SucursalResponse;
import dsw.sigvidbackend.service.SucursalService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private static final Logger logger = LoggerFactory.getLogger(SucursalController.class);
    private final SucursalService sucursalService;

    @GetMapping
    public ResponseEntity<List<SucursalResponse>> listarTodas() {
        return ResponseEntity.ok(sucursalService.listarSucursales());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SucursalResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sucursalService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<SucursalResponse> crearSucursal(@Valid @RequestBody SucursalRequest request) {
        logger.info(">crearSucursal: {}", request);
        return ResponseEntity.ok(sucursalService.guardarSucursal(request));
    }
}