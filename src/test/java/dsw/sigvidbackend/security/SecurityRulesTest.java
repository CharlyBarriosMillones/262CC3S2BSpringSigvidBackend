package dsw.sigvidbackend.security;

import dsw.sigvidbackend.controller.*;
import dsw.sigvidbackend.repository.UsuarioRepository;
import dsw.sigvidbackend.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PedidoController.class, PagoController.class, StockController.class,
        RecepcionController.class, DashboardController.class, AuthController.class, ProductoController.class})
@Import(SecurityConfig.class)
class SecurityRulesTest {

    @Autowired MockMvc mvc;

    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean UsuarioRepository usuarioRepository;
    @MockitoBean PedidoService pedidoService;
    @MockitoBean PagoService pagoService;
    @MockitoBean StockService stockService;
    @MockitoBean RecepcionService recepcionService;
    @MockitoBean DashboardService dashboardService;
    @MockitoBean AuthService authService;
    @MockitoBean ProductoService productoService;

    @Test
    void sinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/pedidos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/dashboard/pedidos-estado")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteVeDashboardPeroNoPedidosGlobalesNiStock() throws Exception {
        mvc.perform(get("/api/dashboard/pedidos-estado")).andExpect(status().isOk());
        mvc.perform(get("/api/pedidos")).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mvc.perform(get("/api/stock")).andExpect(status().isForbidden());
        mvc.perform(post("/api/productos/save").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void cajeroConsultaPedidosYPagosPeroNoRegistraPedidos() throws Exception {
        mvc.perform(get("/api/pedidos")).andExpect(status().isOk());
        mvc.perform(get("/api/pagos")).andExpect(status().isOk());
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    void vendedorVeDashboardPeroNoPagosNiAlmacenNiRecepcion() throws Exception {
        mvc.perform(get("/api/dashboard/ventas-diarias")).andExpect(status().isOk());
        mvc.perform(get("/api/pagos")).andExpect(status().isForbidden());
        mvc.perform(get("/api/stock")).andExpect(status().isForbidden());
        mvc.perform(get("/api/recepcion/pedidos")).andExpect(status().isForbidden());
        mvc.perform(get("/api/dashboard/stock-categoria")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void almaceneroConsultaStock() throws Exception {
        mvc.perform(get("/api/stock")).andExpect(status().isOk());
        mvc.perform(get("/api/almacenes")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    void pedidoInvalidoDevuelve400ConMensaje() throws Exception {
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentoCliente\":\"ABC\",\"lineas\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void loginPublicoValidaCampos() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"no-es-correo\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}