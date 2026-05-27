package br.gov.seed.relatorios.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            var token = extrairToken(request);
            
            if (token != null && jwtService.validarToken(token)) {
                var usuarioId = jwtService.extrairUsuarioId(token);
                var perfil = jwtService.extrairPerfil(token);
                
                var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + perfil)
                );
                
                var authentication = new UsernamePasswordAuthenticationToken(
                    usuarioId, null, authorities
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Autenticação JWT válida para usuário: {}", usuarioId);
            }
        } catch (Exception e) {
            log.error("Erro ao processar JWT", e);
        }
        
        filterChain.doFilter(request, response);
    }

    private String extrairToken(HttpServletRequest request) {
        var header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}

