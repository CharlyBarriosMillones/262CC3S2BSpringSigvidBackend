package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.ModuloRequest;
import dsw.sigvidbackend.dto.ModuloResponse;
import dsw.sigvidbackend.model.Modulo;
import dsw.sigvidbackend.repository.ModuloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuloService {

    private final ModuloRepository moduloRepository;

    public List<ModuloResponse> list(ModuloRequest filter) {
        String desc = (filter != null && filter.getDescripcion() != null && !filter.getDescripcion().isBlank())
                ? filter.getDescripcion().trim()
                : null;
        return ModuloResponse.fromEntities(moduloRepository.findByFilter(desc));
    }

    public List<ModuloResponse> listByRol(Long idRol) {
        return ModuloResponse.fromEntities(moduloRepository.findByRolId(idRol));
    }

    public ModuloResponse find(Integer id) {
        return moduloRepository.findById(id)
                .map(ModuloResponse::fromEntity)
                .orElse(null);
    }

    @Transactional
    public ModuloResponse save(ModuloRequest request) {
        Modulo modulo = ModuloRequest.toEntity(request);
        modulo = moduloRepository.saveAndFlush(modulo);
        return ModuloResponse.fromEntity(modulo);
    }
}