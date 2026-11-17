package dsw.sigvidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {
    private Long idProducto;
    private String codigoProducto;
    private String nombre;
    private BigDecimal precioReferencial;
    private Integer stockMinimo;
    private Long idCategoria;
    private Long idMarca;
    private Long idProveedor;
    private String cepa;
    private String descripcion;
    private String maridaje;
    private String imagenUrl;
    private Boolean activo;
}