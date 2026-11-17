package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.RolRequest;
import dsw.sigvidbackend.dto.RolResponse;
import dsw.sigvidbackend.model.Rol;
import dsw.sigvidbackend.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    public List<RolResponse> listarRoles() {
        return RolResponse.fromEntities(rolRepository.findAll());
    }

    public List<RolResponse> list(RolRequest filter) {
        String nombre = (filter != null && filter.getNombre() != null && !filter.getNombre().isBlank())
                ? filter.getNombre().trim()
                : null;
        return RolResponse.fromEntities(rolRepository.findByFilter(nombre));
    }

    public RolResponse obtenerPorId(Long id) {
        return rolRepository.findById(id)
                .map(RolResponse::fromEntity)
                .orElseThrow(() -> new NoSuchElementException("Rol no encontrado con ID: " + id));
    }

    @Transactional
    public RolResponse guardar(RolRequest request) {
        Rol rol = RolRequest.toEntity(request);
        rol = rolRepository.saveAndFlush(rol);
        return RolResponse.fromEntity(rol);
    }
}