package dsw.sigvidbackend.dto;

import dsw.sigvidbackend.model.Producto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {
    private Long idProducto;
    private String codigoProducto;
    private String nombre;
    private BigDecimal precioReferencial;
    private Integer stockMinimo;
    private Long idCategoria;
    private String nombreCategoria;
    private Long idMarca;
    private String nombreMarca;
    private Long idProveedor;
    private String razonSocialProveedor;
    private String cepa;
    private String descripcion;
    private String maridaje;
    private String imagenUrl;
    private Boolean activo;

    public static ProductoResponse fromEntity(Producto p) {
        return ProductoResponse.builder()
                .idProducto(p.getIdProducto())
                .codigoProducto(p.getCodigoProducto())
                .nombre(p.getNombre())
                .precioReferencial(p.getPrecioReferencial())
                .stockMinimo(p.getStockMinimo())
                .idCategoria(p.getCategoria() != null ? p.getCategoria().getIdCategoria() : null)
                .nombreCategoria(p.getCategoria() != null ? p.getCategoria().getNombre() : null)
                .idMarca(p.getMarca() != null ? p.getMarca().getIdMarca() : null)
                .nombreMarca(p.getMarca() != null ? p.getMarca().getNombre() : null)
                .idProveedor(p.getProveedor() != null ? p.getProveedor().getIdProveedor() : null)
                .razonSocialProveedor(p.getProveedor() != null ? p.getProveedor().getRazonSocial() : null)
                .cepa(p.getCepa())
                .descripcion(p.getDescripcion())
                .maridaje(p.getMaridaje())
                .imagenUrl(p.getImagenUrl())
                .activo(p.getActivo() != null ? p.getActivo() : true)
                .build();
    }

    public static List<ProductoResponse> fromEntities(List<Producto> productos) {
        return productos.stream()
                .map(ProductoResponse::fromEntity)
                .collect(Collectors.toList());
    }
}