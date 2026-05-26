package br.gov.seed.relatorios.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public boolean validarToken(String token) {
        try {
            var secretKey = new SecretKeySpec(
                jwtSecret.getBytes(),
                0,
                jwtSecret.getBytes().length,
                "HmacSHA256"
            );

            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);

            return true;
        } catch (SignatureException e) {
            log.error("Token JWT inválido ou expirado");
            return false;
        }
    }

    public UUID extrairUsuarioId(String token) {
        var claims = extrairClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public String extrairPerfil(String token) {
        var claims = extrairClaims(token);
        return claims.get("perfil", String.class);
    }

    private Claims extrairClaims(String token) {
        var secretKey = new SecretKeySpec(
            jwtSecret.getBytes(),
            0,
            jwtSecret.getBytes().length,
            "HmacSHA256"
        );

        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}

