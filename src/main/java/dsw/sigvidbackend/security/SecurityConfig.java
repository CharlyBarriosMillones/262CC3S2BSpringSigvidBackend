package dsw.sigvidbackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas (Autenticación, Catálogo público y Swagger)
                .requestMatchers("/api/auth/**", "/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**", "/api/marcas/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/docs/**").permitAll()
                // info del backend y salud (actuator) para verificar el despliegue
                .requestMatchers("/", "/actuator/health", "/actuator/health/**").permitAll()

                // Perfil y búsqueda de cliente (disponible para cualquier usuario autenticado)
                .requestMatchers("/api/usuarios/perfil/**", "/api/usuarios/buscar-documento/**").authenticated()
                .requestMatchers("/api/modulos/**", "/api/v1/modulo/**").authenticated()

                // Lectura de roles y sucursales para usuarios autenticados
                .requestMatchers(HttpMethod.GET, "/api/roles/**", "/api/sucursales/**").authenticated()

                // Administración exclusiva para Administrador
                .requestMatchers("/api/roles/**", "/api/sucursales/**", "/api/usuarios/**").hasRole("ADMINISTRADOR")
                // alta/edicion de productos solo Administrador (la lectura es publica)
                .requestMatchers("/api/productos/**").hasRole("ADMINISTRADOR")

                // sprint 3 - almacen y logistica (HU07, HU08, HU09): trabajador de almacen y administrador
                .requestMatchers("/api/stock/**", "/api/almacenes/**", "/api/ubigeos/**", "/api/almacen/**")
                        .hasAnyRole("ADMINISTRADOR", "ALMACENERO")
                // dashboard (charts): administrador y vendedor
                .requestMatchers("/api/dashboard/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                // sprint 3 - confirmar recepcion (HU10): cliente (el pedido se valida contra el token)
                .requestMatchers("/api/recepcion/**").hasRole("CLIENTE")

                // pedidos: el historial propio lo ve cualquier usuario autenticado (cliente)
                .requestMatchers(HttpMethod.GET, "/api/pedidos/cliente").authenticated()
                // consulta de pedidos: vendedor (HU04/HU06), cajero (HU05) y administrador
                .requestMatchers(HttpMethod.GET, "/api/pedidos/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR", "CAJERO")
                // registrar y derivar pedidos: vendedor y administrador
                .requestMatchers("/api/pedidos/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                // pagos y caja: cajero y administrador (HU05)
                .requestMatchers("/api/pagos/**").hasAnyRole("ADMINISTRADOR", "CAJERO")

                // Cualquier otra ruta requiere estar autenticado
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    Map<String, Object> body = new HashMap<>();
                    body.put("timestamp", LocalDateTime.now().toString());
                    body.put("status", 401);
                    body.put("error", "Unauthorized");
                    body.put("message", "Acceso no autorizado. Debe proporcionar un token válido.");
                    new ObjectMapper().writeValue(response.getOutputStream(), body);
                })
                // usuario autenticado pero sin el rol requerido -> 403 en JSON
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    Map<String, Object> body = new HashMap<>();
                    body.put("timestamp", LocalDateTime.now().toString());
                    body.put("status", 403);
                    body.put("error", "Forbidden");
                    body.put("message", "No tiene permisos para realizar esta operación con su rol.");
                    new ObjectMapper().writeValue(response.getOutputStream(), body);
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:4200", "*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}