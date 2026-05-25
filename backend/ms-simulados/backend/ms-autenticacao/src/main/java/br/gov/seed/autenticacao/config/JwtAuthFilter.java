package br.gov.seed.autenticacao.config;

import br.gov.seed.autenticacao.repository.UsuarioRepository;
import br.gov.seed.autenticacao.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String usuarioId = jwtService.extrairUsuarioId(token);

            if (usuarioId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var usuario = usuarioRepository.findById(UUID.fromString(usuarioId)).orElse(null);

                if (usuario != null && jwtService.tokenValido(token, usuario)) {
                    var auth = new UsernamePasswordAuthenticationToken(
                        usuario, null, usuario.getAuthorities()
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception ignored) {
            // Token inválido — deixa o Spring Security retornar 401
        }

        filterChain.doFilter(request, response);
    }
}
