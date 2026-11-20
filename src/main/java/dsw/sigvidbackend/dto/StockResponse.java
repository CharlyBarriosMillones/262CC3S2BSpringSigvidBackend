package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.Stock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// fila de la tabla de stock (HU07): stock actual, minimo y estado OK / Bajo / Crítico
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {
    public static final String OK = "OK";
    public static final String BAJO = "Bajo";
    public static final String CRITICO = "Crítico";

    private Long idStock;
    private Long idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private String nombreCategoria;
    private String nombreMarca;
    private Long idAlmacen;
    private String nombreAlmacen;
    private String idUbigeo;
    private String ubicacion; // departamento / provincia / distrito
    private Integer cantidadActual;
    private Integer stockMinimo;
    private String estado;
    private LocalDateTime fechaUltimaConfirmacion;
    private String usuarioConfirma;
    private String mensaje;

    // regla aprobada: Crítico si actual <= 50% del minimo; Bajo si actual <= minimo; OK en otro caso
    public static String calcularEstado(Integer actual, Integer minimo) {
        int a = actual != null ? actual : 0;
        int m = minimo != null ? minimo : 0;
        if (a * 2 <= m) {
            return CRITICO;
        }
        if (a <= m) {
            return BAJO;
        }
        return OK;
    }

    public static StockResponse fromEntity(Stock s) {
        var p = s.getProducto();
        var a = s.getAlmacen();
        var u = a.getUbigeo();
        return StockResponse.builder()
                .idStock(s.getIdStock())
                .idProducto(p.getIdProducto())
                .codigoProducto(p.getCodigoProducto())
                .nombreProducto(p.getNombre())
                .nombreCategoria(p.getCategoria() != null ? p.getCategoria().getNombre() : null)
                .nombreMarca(p.getMarca() != null ? p.getMarca().getNombre() : null)
                .idAlmacen(a.getIdAlmacen())
                .nombreAlmacen(a.getNombre())
                .idUbigeo(u != null ? u.getIdUbigeo() : null)
                .ubicacion(u != null ? u.getDepartamento() + " / " + u.getProvincia() + " / " + u.getDistrito() : null)
                .cantidadActual(s.getCantidadActual())
                .stockMinimo(p.getStockMinimo())
                .estado(calcularEstado(s.getCantidadActual(), p.getStockMinimo()))
                .fechaUltimaConfirmacion(s.getFechaUltimaConfirmacion())
                .usuarioConfirma(s.getUsuarioConfirma() != null
                        ? s.getUsuarioConfirma().getNombre() + " " + s.getUsuarioConfirma().getApellido() : null)
                .build();
    }

    public static List<StockResponse> fromEntities(List<Stock> lista) {
        return lista.stream().map(StockResponse::fromEntity).toList();
    }
}
