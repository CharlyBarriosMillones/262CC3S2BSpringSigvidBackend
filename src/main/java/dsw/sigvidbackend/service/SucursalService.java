package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.SucursalRequest;
import dsw.sigvidbackend.dto.SucursalResponse;
import dsw.sigvidbackend.model.Sucursal;
import dsw.sigvidbackend.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SucursalService {

    private final SucursalRepository sucursalRepository;

    public List<SucursalResponse> listarSucursales() {
        return SucursalResponse.fromEntities(sucursalRepository.findAll());
    }

    public SucursalResponse obtenerPorId(Long id) {
        return sucursalRepository.findById(id)
                .map(SucursalResponse::fromEntity)
                .orElseThrow(() -> new NoSuchElementException("Sucursal no encontrada con ID: " + id));
    }

    @Transactional
    public SucursalResponse guardarSucursal(SucursalRequest request) {
        Sucursal sucursal = SucursalRequest.toEntity(request);
        sucursal = sucursalRepository.saveAndFlush(sucursal);
        return SucursalResponse.fromEntity(sucursal);
    }
}