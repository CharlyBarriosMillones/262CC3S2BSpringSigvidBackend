package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.AlmacenResponse;
import dsw.sigvidbackend.dto.StockResponse;
import dsw.sigvidbackend.model.Stock;
import dsw.sigvidbackend.model.Ubigeo;
import dsw.sigvidbackend.model.Usuario;
import dsw.sigvidbackend.repository.AlmacenRepository;
import dsw.sigvidbackend.repository.StockRepository;
import dsw.sigvidbackend.repository.UbigeoRepository;
import dsw.sigvidbackend.util.Patrones;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

// HU07 - consultar stock en tiempo real por producto y almacen
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final AlmacenRepository almacenRepository;
    private final UbigeoRepository ubigeoRepository;

    // buscador por codigo, nombre o categoria + filtros por categoria, almacen y ubigeo
    public List<StockResponse> consultarStock(String termino, Long idCategoria, Long idAlmacen, String idUbigeo, String estado) {
        if (termino != null && !termino.isBlank() && !termino.trim().matches(Patrones.BUSQUEDA)) {
            throw new IllegalArgumentException("La búsqueda solo admite letras, números, espacios y guiones (máx. 50)");
        }
        String t = (termino != null && !termino.isBlank()) ? termino.trim().toLowerCase() : null;
        return stockRepository.findAllByOrderByProducto_CodigoProductoAscAlmacen_NombreAsc().stream()
                .filter(s -> s.getProducto().getActivo() == null || s.getProducto().getActivo())
                .filter(s -> t == null
                        || s.getProducto().getCodigoProducto().toLowerCase().contains(t)
                        || s.getProducto().getNombre().toLowerCase().contains(t)
                        || (s.getProducto().getCategoria() != null && s.getProducto().getCategoria().getNombre().toLowerCase().contains(t)))
                .filter(s -> idCategoria == null || (s.getProducto().getCategoria() != null
                        && idCategoria.equals(s.getProducto().getCategoria().getIdCategoria())))
                .filter(s -> idAlmacen == null || idAlmacen.equals(s.getAlmacen().getIdAlmacen()))
                .filter(s -> idUbigeo == null || idUbigeo.isBlank()
                        || (s.getAlmacen().getUbigeo() != null && idUbigeo.equals(s.getAlmacen().getUbigeo().getIdUbigeo())))
                .map(StockResponse::fromEntity)
                .filter(r -> estado == null || estado.isBlank() || estado.equalsIgnoreCase(r.getEstado()))
                .toList();
    }

    // boton "Confirmar stock": registra quien y cuando valido la disponibilidad
    @Transactional
    public StockResponse confirmarStock(Long idStock, Usuario trabajador) {
        Stock stock = stockRepository.findById(idStock)
                .orElseThrow(() -> new NoSuchElementException("Registro de stock no encontrado con ID: " + idStock));
        stock.setFechaUltimaConfirmacion(LocalDateTime.now());
        stock.setUsuarioConfirma(trabajador);
        stock = stockRepository.saveAndFlush(stock);
        StockResponse r = StockResponse.fromEntity(stock);
        r.setMensaje("Stock confirmado: " + r.getCantidadActual() + " unidades de " + r.getNombreProducto()
                + " disponibles en " + r.getNombreAlmacen() + " (estado " + r.getEstado() + ").");
        return r;
    }

    public List<AlmacenResponse> listarAlmacenes() {
        return AlmacenResponse.fromEntities(almacenRepository.findByActivoTrueOrderByNombreAsc());
    }

    public List<Ubigeo> listarUbigeos() {
        return ubigeoRepository.findAllByOrderByDepartamentoAscProvinciaAscDistritoAsc();
    }
}
