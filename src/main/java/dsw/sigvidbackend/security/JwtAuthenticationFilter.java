package dsw.sigvidbackend.security;

import dsw.sigvidbackend.model.Usuario;
import dsw.sigvidbackend.repository.UsuarioRepository;
import dsw.sigvidbackend.service.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String email = null;
        String jwt = null;

        // verificams si la peticion trae un token bearer
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                email = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // si el token es invalido o expiro, continuamos sin autenticar
                logger.warn("Error al extraer usuario del token JWT: " + e.getMessage());
            }
        }

        // si hemos obtenido el email y el usuario aun no esta autenticado en este hilo
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

            if (usuarioOpt.isPresent() && jwtUtil.validateToken(jwt, email)) {
                Usuario usuario = usuarioOpt.get();

                // asignamos el rol de la base de datos como autoridad de spring security
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                        "ROLE_" + usuario.getRol().getNombre().toUpperCase()
                );

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        usuario, null, Collections.singletonList(authority)
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // coontinuamos con la peticion
        filterChain.doFilter(request, response);
    }
}