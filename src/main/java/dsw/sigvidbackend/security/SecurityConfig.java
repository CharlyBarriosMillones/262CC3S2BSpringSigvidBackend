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
                // Rutas públicas (Autenticación, Catálogo público, Swagger y Health)
                .requestMatchers("/api/auth/**", "/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**", "/api/marcas/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/docs/**").permitAll()
                .requestMatchers("/", "/actuator/health", "/actuator/health/**").permitAll()

                // Dashboard inicial, perfil, búsqueda de cliente y módulos (cualquier usuario autenticado)
                .requestMatchers("/api/dashboard/**").authenticated()
                .requestMatchers("/api/usuarios/perfil/**", "/api/usuarios/buscar-documento/**").authenticated()
                .requestMatchers("/api/modulos/**", "/api/v1/modulo/**").authenticated()

                // Lectura de roles y sucursales para usuarios autenticados
                .requestMatchers(HttpMethod.GET, "/api/roles/**", "/api/sucursales/**").authenticated()

                // Administración exclusiva para Administrador
                .requestMatchers("/api/roles/**", "/api/sucursales/**", "/api/usuarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/productos/**").hasRole("ADMINISTRADOR")

                // Sprint 3 - Almacén y logística (HU07, HU08, HU09)
                .requestMatchers("/api/stock/**", "/api/almacenes/**", "/api/ubigeos/**", "/api/almacen/**")
                        .hasAnyRole("ADMINISTRADOR", "ALMACENERO")

                // Sprint 3 - Confirmar recepción (HU10): cliente dueño del pedido
                .requestMatchers("/api/recepcion/**").hasRole("CLIENTE")

                // Pedidos y pagos (Sprints 1 y 2)
                .requestMatchers(HttpMethod.GET, "/api/pedidos/cliente").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/pedidos/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR", "CAJERO")
                .requestMatchers("/api/pedidos/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                .requestMatchers("/api/pagos/**").hasAnyRole("ADMINISTRADOR", "CAJERO")

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