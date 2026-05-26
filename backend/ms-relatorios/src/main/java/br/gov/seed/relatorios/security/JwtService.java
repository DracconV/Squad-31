package br.gov.seed.relatorios.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public boolean validarToken(String token) {
        try {
            extrairClaims(token);
            return true;
        } catch (SignatureException e) {
            log.error("Assinatura JWT invalida");
            return false;
        } catch (Exception e) {
            log.error("Token JWT invalido ou expirado: {}", e.getMessage());
            return false;
        }
    }

    public UUID extrairUsuarioId(String token) {
        return UUID.fromString(extrairClaims(token).getSubject());
    }

    public String extrairPerfil(String token) {
        return extrairClaims(token).get("perfil", String.class);
    }

    private Claims extrairClaims(String token) {
        var secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
