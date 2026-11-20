package dsw.sigvidbackend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

// pruebas de las validaciones (regex y restricciones) de los DTOs, sin base de datos
class DtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    private Set<String> camposInvalidos(Object dto) {
        Set<ConstraintViolation<Object>> v = validator.validate(dto);
        return v.stream().map(c -> c.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    private RegisterRequestDTO registroValido() {
        RegisterRequestDTO r = new RegisterRequestDTO();
        r.setNombre("José María");
        r.setApellido("Peña O'Neil");
        r.setDni("12345678");
        r.setEmail("jose@correo.pe");
        r.setTelefono("987654321");
        r.setPassword("secreto1");
        r.setRuc("20556677881");
        return r;
    }

    @Test
    void registroValidoNoTieneErrores() {
        assertTrue(camposInvalidos(registroValido()).isEmpty());
    }

    @Test
    void registroRechazaDniTelefonoRucYNombreInvalidos() {
        RegisterRequestDTO r = registroValido();
        r.setDni("1234567");
        r.setTelefono("98-765");
        r.setRuc("12345678901");
        r.setNombre("J0sé");
        r.setEmail("no-es-correo");
        r.setPassword("123");
        Set<String> campos = camposInvalidos(r);
        assertTrue(campos.containsAll(List.of("dni", "telefono", "ruc", "nombre", "email", "password")), campos.toString());
    }

    @Test
    void registroRechazaHtmlEnDireccion() {
        RegisterRequestDTO r = registroValido();
        r.setDireccion("<script>alert(1)</script>");
        assertTrue(camposInvalidos(r).contains("direccion"));
    }

    @Test
    void pedidoValidaLineasDescuentoYDocumentos() {
        PedidoRequest p = PedidoRequest.builder()
                .documentoCliente("12345678")
                .clienteRuc("10123456789")
                .tipoDescuento("porcentaje")
                .valorDescuento(BigDecimal.TEN)
                .lineas(List.of(LineaPedidoRequest.builder().idProducto(1L).cantidad(2).build()))
                .build();
        assertTrue(camposInvalidos(p).isEmpty());

        PedidoRequest malo = PedidoRequest.builder()
                .documentoCliente("ABC")
                .tipoDescuento("REGALO")
                .valorDescuento(new BigDecimal("-1"))
                .lineas(List.of(LineaPedidoRequest.builder().idProducto(null).cantidad(0).build()))
                .build();
        Set<String> campos = camposInvalidos(malo);
        assertTrue(campos.containsAll(List.of("documentoCliente", "tipoDescuento", "valorDescuento",
                "lineas[0].idProducto", "lineas[0].cantidad")), campos.toString());

        PedidoRequest vacio = PedidoRequest.builder().lineas(List.of()).build();
        assertTrue(camposInvalidos(vacio).contains("lineas"));
    }

    @Test
    void pagoValidaMetodoYVoucher() {
        PagoRequest ok = PagoRequest.builder().idPedido(1L).nombreMetodo("Tarjeta").codigoVoucher("OP-12345").build();
        assertTrue(camposInvalidos(ok).isEmpty());

        PagoRequest malo = PagoRequest.builder().nombreMetodo("Bitcoin").codigoVoucher("x").montoRecibido(new BigDecimal("-5")).build();
        Set<String> campos = camposInvalidos(malo);
        assertTrue(campos.containsAll(List.of("idPedido", "nombreMetodo", "codigoVoucher", "montoRecibido")), campos.toString());
    }

    @Test
    void productoValidaCodigoYPrecio() {
        ProductoRequest ok = ProductoRequest.builder().codigoProducto("VIN-004").nombre("Syrah")
                .precioReferencial(new BigDecimal("50")).idCategoria(1L).idMarca(1L).build();
        assertTrue(camposInvalidos(ok).isEmpty());

        ProductoRequest malo = ProductoRequest.builder().codigoProducto("vin4").nombre("")
                .precioReferencial(BigDecimal.ZERO).build();
        Set<String> campos = camposInvalidos(malo);
        assertTrue(campos.containsAll(List.of("codigoProducto", "nombre", "precioReferencial", "idCategoria", "idMarca")), campos.toString());
    }

    @Test
    void loginExigeCorreoYPassword() {
        Set<String> campos = camposInvalidos(new LoginRequestDTO("", ""));
        assertTrue(campos.containsAll(List.of("email", "password")));
    }
}
