package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.AsignacionRequest;
import dsw.sigvidbackend.dto.GuiaDespachoResponse;
import dsw.sigvidbackend.dto.RecepcionRequest;
import dsw.sigvidbackend.dto.StockResponse;
import dsw.sigvidbackend.model.*;
import dsw.sigvidbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

// pruebas unitarias (sin BD) de las reglas de negocio del sprint 3
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Sprint3ServiceTest {

    @Mock PedidoRepository pedidoRepository;
    @Mock LineaPedidoRepository lineaPedidoRepository;
    @Mock FacturaElectronicaRepository facturaElectronicaRepository;
    @Mock EstadoPedidoRepository estadoPedidoRepository;
    @Mock AlmacenRepository almacenRepository;
    @Mock StockRepository stockRepository;
    @Mock GuiaDespachoRepository guiaDespachoRepository;
    @Mock DetalleGuiaRepository detalleGuiaRepository;
    @Mock RecepcionPedidoRepository recepcionPedidoRepository;

    @InjectMocks AlmacenService almacenService;

    private Usuario cliente;
    private Usuario almacenero;
    private Producto vino;
    private Almacen almacen;
    private Pedido pedido;

    @BeforeEach
    void datos() {
        cliente = new Usuario();
        cliente.setIdUsuario(10L);
        cliente.setNombre("Ana");
        cliente.setApellido("Ríos");
        cliente.setEmail("ana@correo.pe");
        cliente.setDni("12345678");
        almacenero = new Usuario();
        almacenero.setIdUsuario(20L);
        almacenero.setNombre("Luis");
        almacenero.setApellido("Paz");
        vino = Producto.builder().idProducto(1L).codigoProducto("VIN-001").nombre("Malbec")
                .precioReferencial(BigDecimal.TEN).stockMinimo(15).build();
        almacen = Almacen.builder().idAlmacen(1L).nombre("Central").ubigeo(new Ubigeo("150103", "LIMA", "LIMA", "ATE")).build();
        pedido = Pedido.builder().idPedido(5L).codigoPedido("PED-2026-0005")
                .estado(EstadoPedido.builder().idEstado(3L).nombre("En preparación").build())
                .cliente(cliente).sucursal(Sucursal.builder().idSucursal(1L).nombre("Sede").build()).build();
        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedido));
        when(almacenRepository.findById(1L)).thenReturn(Optional.of(almacen));
        when(lineaPedidoRepository.findByPedido_IdPedido(5L)).thenReturn(List.of(
                LineaPedido.builder().producto(vino).cantidad(4).build(),
                LineaPedido.builder().producto(vino).cantidad(6).build()));
        when(guiaDespachoRepository.saveAndFlush(any())).thenAnswer(i -> {
            GuiaDespacho g = i.getArgument(0);
            g.setIdGuia(100L);
            return g;
        });
        when(detalleGuiaRepository.saveAllAndFlush(anyList())).thenAnswer(i -> i.getArgument(0));
    }

    private void stock(int cantidad) {
        when(stockRepository.findByProducto_IdProductoAndAlmacen_IdAlmacen(1L, 1L))
                .thenReturn(Optional.of(Stock.builder().idStock(1L).producto(vino).almacen(almacen).cantidadActual(cantidad).build()));
    }

    private AsignacionRequest asignar(int cantidad) {
        return AsignacionRequest.builder().idAlmacen(1L)
                .lineas(List.of(AsignacionRequest.Linea.builder().idProducto(1L).cantidadAsignada(cantidad).build())).build();
    }

    @Test
    void estadoDeStockSegunRegla() {
        assertEquals("Crítico", StockResponse.calcularEstado(7, 15));
        assertEquals("Bajo", StockResponse.calcularEstado(8, 15));
        assertEquals("Bajo", StockResponse.calcularEstado(15, 15));
        assertEquals("OK", StockResponse.calcularEstado(16, 15));
        assertEquals("Crítico", StockResponse.calcularEstado(0, 0));
    }

    @Test
    void asignacionSumaLineasYGeneraGuiaConQr() {
        stock(50);
        GuiaDespachoResponse r = almacenService.asignarProductos(5L, asignar(10), almacenero);
        assertEquals("Preliminar", r.getEstadoGuia());
        assertEquals(1, r.getDetalles().size());
        assertEquals(10, r.getDetalles().get(0).getCantidadSolicitada());
        assertTrue(r.getDetalles().get(0).getCodigoQr().startsWith("SIGVID|GD-"));
        assertTrue(r.getDetalles().get(0).getCodigoQr().endsWith("|VIN-001|10"));
    }

    @Test
    void asignacionParcialPermitidaSiHayStockInsuficiente() {
        stock(3);
        GuiaDespachoResponse r = almacenService.asignarProductos(5L, asignar(3), almacenero);
        assertTrue(r.getNotificacion().contains("parcial"));
    }

    @Test
    void asignacionRechazaMasQueElStockOLoSolicitado() {
        stock(3);
        assertThrows(IllegalArgumentException.class, () -> almacenService.asignarProductos(5L, asignar(5), almacenero));
        stock(100);
        assertThrows(IllegalArgumentException.class, () -> almacenService.asignarProductos(5L, asignar(11), almacenero));
    }

    @Test
    void asignacionDescuentaLoReservadoEnGuiasPreliminares() {
        stock(12);
        when(detalleGuiaRepository.findByGuia_EstadoAndGuia_Almacen_IdAlmacenAndProducto_IdProducto("Preliminar", 1L, 1L))
                .thenReturn(List.of(DetalleGuia.builder().producto(vino).cantidadAsignada(5).build()));
        // disponible = 12 - 5 reservados = 7
        assertThrows(IllegalArgumentException.class, () -> almacenService.asignarProductos(5L, asignar(8), almacenero));
        assertEquals(7, almacenService.obtenerDetalle(5L, 1L).getLineas().get(0).getStockDisponible());
    }

    @Test
    void escaneoExigeQrCorrecto() {
        GuiaDespacho guia = GuiaDespacho.builder().idGuia(100L).codigoGuia("GD-000001").pedido(pedido)
                .almacen(almacen).trabajador(almacenero).estado("Preliminar").build();
        DetalleGuia d = DetalleGuia.builder().idDetalle(1L).guia(guia).producto(vino)
                .cantidadSolicitada(10).cantidadAsignada(10).codigoQr("SIGVID|GD-000001|PED-2026-0005|VIN-001|10").escaneado(false).build();
        when(guiaDespachoRepository.findById(100L)).thenReturn(Optional.of(guia));
        when(detalleGuiaRepository.findById(1L)).thenReturn(Optional.of(d));
        when(detalleGuiaRepository.findByGuia_IdGuiaOrderByIdDetalleAsc(100L)).thenReturn(List.of(d));
        assertThrows(IllegalArgumentException.class, () -> almacenService.registrarEscaneo(100L, 1L, null));
        assertThrows(IllegalArgumentException.class, () -> almacenService.registrarEscaneo(100L, 1L, "otro"));
        GuiaDespachoResponse r = almacenService.registrarEscaneo(100L, 1L, d.getCodigoQr());
        assertEquals(1, r.getItemsEscaneados());
    }

    @Test
    void asignacionRechazaPedidoNoDerivado() {
        pedido.setEstado(EstadoPedido.builder().nombre("Pagado").build());
        stock(50);
        assertThrows(IllegalArgumentException.class, () -> almacenService.asignarProductos(5L, asignar(10), almacenero));
    }

    @Test
    void salidaExigeTodoEscaneadoYDescuentaStock() {
        GuiaDespacho guia = GuiaDespacho.builder().idGuia(100L).codigoGuia("GD-000001").pedido(pedido)
                .almacen(almacen).trabajador(almacenero).estado("Preliminar").build();
        DetalleGuia d = DetalleGuia.builder().idDetalle(1L).guia(guia).producto(vino)
                .cantidadSolicitada(10).cantidadAsignada(10).codigoQr("x").escaneado(false).build();
        Stock s = Stock.builder().idStock(1L).producto(vino).almacen(almacen).cantidadActual(50).build();
        when(guiaDespachoRepository.findById(100L)).thenReturn(Optional.of(guia));
        when(detalleGuiaRepository.findByGuia_IdGuiaOrderByIdDetalleAsc(100L)).thenReturn(List.of(d));
        when(stockRepository.findByProducto_IdProductoAndAlmacen_IdAlmacen(1L, 1L)).thenReturn(Optional.of(s));
        when(estadoPedidoRepository.findByNombreIgnoreCase("En tránsito"))
                .thenReturn(Optional.of(EstadoPedido.builder().idEstado(4L).nombre("En tránsito").build()));

        assertThrows(IllegalArgumentException.class, () -> almacenService.confirmarSalida(100L));

        d.setEscaneado(true);
        GuiaDespachoResponse r = almacenService.confirmarSalida(100L);
        assertEquals(40, s.getCantidadActual());
        assertEquals("Despachada", r.getEstadoGuia());
        assertEquals("En tránsito", pedido.getEstado().getNombre());
    }

    @Test
    void recepcionSoloDelClienteDuenoYConFirmaCuandoSeElige() {
        RecepcionService recepcion = new RecepcionService(pedidoRepository, estadoPedidoRepository,
                guiaDespachoRepository, detalleGuiaRepository, recepcionPedidoRepository);
        pedido.setEstado(EstadoPedido.builder().nombre("En tránsito").build());
        GuiaDespacho guia = GuiaDespacho.builder().idGuia(100L).codigoGuia("GD-000001").pedido(pedido)
                .almacen(almacen).trabajador(almacenero).estado("Despachada").build();
        DetalleGuia d = DetalleGuia.builder().idDetalle(1L).guia(guia).producto(vino)
                .cantidadSolicitada(10).cantidadAsignada(10).codigoQr("x").escaneado(true).build();
        when(guiaDespachoRepository.findByPedido_IdPedido(5L)).thenReturn(Optional.of(guia));
        when(detalleGuiaRepository.findByGuia_IdGuiaOrderByIdDetalleAsc(100L)).thenReturn(List.of(d));
        when(estadoPedidoRepository.findByNombreIgnoreCase("Entregado"))
                .thenReturn(Optional.of(EstadoPedido.builder().idEstado(5L).nombre("Entregado").build()));

        RecepcionRequest firmaSinImagen = RecepcionRequest.builder().tipo("FIRMA")
                .lineas(List.of(RecepcionRequest.Linea.builder().idDetalle(1L).cantidadRecibida(10).build())).build();
        assertThrows(IllegalArgumentException.class, () -> recepcion.confirmarRecepcion(5L, firmaSinImagen, cliente));

        Usuario otro = new Usuario();
        otro.setIdUsuario(99L);
        RecepcionRequest conformidad = RecepcionRequest.builder().tipo("CONFORMIDAD")
                .lineas(List.of(RecepcionRequest.Linea.builder().idDetalle(1L).cantidadRecibida(9).build())).build();
        assertThrows(java.util.NoSuchElementException.class, () -> recepcion.confirmarRecepcion(5L, conformidad, otro));

        GuiaDespachoResponse r = recepcion.confirmarRecepcion(5L, conformidad, cliente);
        assertEquals("Entregado", pedido.getEstado().getNombre());
        assertNotNull(pedido.getFechaEntrega());
        assertEquals(9, d.getCantidadRecibida());
        assertTrue(r.getNotificacion().contains("Diferencias"));
        verify(recepcionPedidoRepository).saveAndFlush(any(RecepcionPedido.class));
    }
}
