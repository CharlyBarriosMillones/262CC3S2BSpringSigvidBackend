package dsw.sigvidbackend.service;

import dsw.sigvidbackend.dto.GuiaDespachoResponse;
import dsw.sigvidbackend.dto.RecepcionRequest;
import dsw.sigvidbackend.model.*;
import dsw.sigvidbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

// HU10 - el cliente confirma la recepcion de su pedido (conformidad o firma digital)
@Service
@RequiredArgsConstructor
public class RecepcionService {

    static final String EN_TRANSITO = "En tránsito";
    static final String ENTREGADO = "Entregado";

    private final PedidoRepository pedidoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final GuiaDespachoRepository guiaDespachoRepository;
    private final DetalleGuiaRepository detalleGuiaRepository;
    private final RecepcionPedidoRepository recepcionPedidoRepository;

    // pedidos del cliente autenticado que estan "En tránsito", con los productos enviados
    public List<GuiaDespachoResponse> listarPedidosEnTransito(Usuario cliente) {
        return guiaDespachoRepository
                .findByPedido_Cliente_EmailAndPedido_Estado_NombreIgnoreCaseOrderByFechaGeneracionDesc(cliente.getEmail(), EN_TRANSITO)
                .stream()
                .map(g -> GuiaDespachoResponse.fromEntity(g, detalleGuiaRepository.findByGuia_IdGuiaOrderByIdDetalleAsc(g.getIdGuia()), null))
                .toList();
    }

    @Transactional
    public GuiaDespachoResponse confirmarRecepcion(Long idPedido, RecepcionRequest request, Usuario cliente) {
        // solo el cliente dueño del pedido puede confirmarlo; un pedido ajeno se responde como no encontrado (404)
        Pedido pedido = pedidoRepository.findById(idPedido)
                .filter(p -> p.getCliente().getIdUsuario().equals(cliente.getIdUsuario()))
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado con ID: " + idPedido));
        if (!EN_TRANSITO.equalsIgnoreCase(pedido.getEstado().getNombre())) {
            throw new IllegalArgumentException("Solo se confirma la recepción de pedidos 'En tránsito'; el pedido "
                    + pedido.getCodigoPedido() + " está en '" + pedido.getEstado().getNombre() + "'");
        }
        if (recepcionPedidoRepository.existsByPedido_IdPedido(idPedido)) {
            throw new IllegalArgumentException("La recepción del pedido " + pedido.getCodigoPedido() + " ya fue confirmada");
        }
        boolean conFirma = RecepcionPedido.FIRMA.equals(request.getTipo());
        if (conFirma && (request.getFirmaDigital() == null || request.getFirmaDigital().isBlank())) {
            throw new IllegalArgumentException("Debe dibujar su firma digital para confirmar con firma");
        }

        GuiaDespacho guia = guiaDespachoRepository.findByPedido_IdPedido(idPedido)
                .orElseThrow(() -> new NoSuchElementException("El pedido " + pedido.getCodigoPedido() + " no tiene guía de despacho"));
        List<DetalleGuia> detalles = detalleGuiaRepository.findByGuia_IdGuiaOrderByIdDetalleAsc(guia.getIdGuia());

        // verificacion de cantidades recibidas por item de la guia
        Map<Long, Integer> recibidas = new HashMap<>();
        for (RecepcionRequest.Linea l : request.getLineas()) {
            if (recibidas.put(l.getIdDetalle(), l.getCantidadRecibida()) != null) {
                throw new IllegalArgumentException("El item " + l.getIdDetalle() + " está repetido");
            }
        }
        List<String> diferencias = new ArrayList<>();
        for (DetalleGuia d : detalles) {
            Integer cant = recibidas.remove(d.getIdDetalle());
            if (cant == null) {
                throw new IllegalArgumentException("Falta verificar la cantidad recibida de " + d.getProducto().getCodigoProducto());
            }
            if (cant > d.getCantidadAsignada()) {
                throw new IllegalArgumentException("La cantidad recibida de " + d.getProducto().getCodigoProducto()
                        + " (" + cant + ") supera la enviada (" + d.getCantidadAsignada() + ")");
            }
            if (cant < d.getCantidadAsignada()) {
                diferencias.add(d.getProducto().getCodigoProducto() + ": recibido " + cant + " de " + d.getCantidadAsignada());
            }
            d.setCantidadRecibida(cant);
        }
        if (!recibidas.isEmpty()) {
            throw new IllegalArgumentException("Hay items que no pertenecen a la guía del pedido: " + recibidas.keySet());
        }
        detalles = detalleGuiaRepository.saveAllAndFlush(detalles);

        recepcionPedidoRepository.saveAndFlush(RecepcionPedido.builder()
                .pedido(pedido)
                .tipo(request.getTipo())
                .firmaDigital(conFirma ? request.getFirmaDigital() : null)
                .observaciones(request.getObservaciones())
                .fecha(LocalDateTime.now())
                .build());

        pedido.setEstado(estadoPedidoRepository.findByNombreIgnoreCase(ENTREGADO)
                .orElseGet(() -> estadoPedidoRepository.findById(5L)
                        .orElseThrow(() -> new NoSuchElementException("Estado 'Entregado' no encontrado en la base de datos"))));
        pedido.setFechaEntrega(LocalDateTime.now());
        pedidoRepository.saveAndFlush(pedido);

        // notificacion de entrega a la empresa (10.6)
        String notificacion = "Notificación de entrega enviada a la empresa: el cliente " + cliente.getNombre() + " "
                + cliente.getApellido() + " confirmó la recepción del pedido " + pedido.getCodigoPedido()
                + (conFirma ? " con firma digital" : " con conformidad")
                + (diferencias.isEmpty() ? "." : ". Diferencias reportadas: " + String.join("; ", diferencias) + ".");
        return GuiaDespachoResponse.fromEntity(guia, detalles, notificacion);
    }
}
