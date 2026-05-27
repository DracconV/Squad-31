package br.gov.seed.autenticacao.service;

import br.gov.seed.autenticacao.dto.AuthDTO.*;
import br.gov.seed.autenticacao.entity.ResetToken;
import br.gov.seed.autenticacao.entity.Usuario;
import br.gov.seed.autenticacao.repository.ResetTokenRepository;
import br.gov.seed.autenticacao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.matricula(), request.senha())
        );

        Usuario usuario = usuarioRepository.findByMatricula(request.matricula())
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        String token = jwtService.gerarToken(usuario);

        auditLogService.registrar(usuario.getId(), "LOGIN", "usuario", usuario.getId(),
                java.util.Map.of("perfil", usuario.getPerfil().name()));

        return new LoginResponse(
                token,
                "Bearer",
                usuario.getId().toString(),
                usuario.getNome(),
                usuario.getPerfil().name(),
                usuario.getPrimeiroAcesso()
        );
    }

    @Transactional
    public void primeiroAcesso(PrimeiroAcessoRequest request) {
        Usuario usuario = usuarioRepository.findByMatricula(request.matricula())
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        if (!usuario.getPrimeiroAcesso()) {
            throw new RuntimeException("Primeiro acesso ja realizado");
        }

        if (!passwordEncoder.matches(request.senhaTemporaria(), usuario.getSenhaHash())) {
            throw new RuntimeException("Senha temporaria incorreta");
        }

        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        usuario.setPrimeiroAcesso(false);
        usuarioRepository.save(usuario);

        auditLogService.registrar(usuario.getId(), "PRIMEIRO_ACESSO", "usuario", usuario.getId(), null);
    }

    /**
     * Gera um token de redefinição de senha vinculado à matrícula.
     * Como não há serviço de e-mail, o token é retornado diretamente
     * para uso pelo admin ou por fluxo de integração futura.
     */
    @Transactional
    public RedefinicaoResponse solicitarRedefinicao(SolicitarRedefinicaoRequest request) {
        Usuario usuario = usuarioRepository.findByMatricula(request.matricula())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Token: 64 chars hex
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        token = token.substring(0, 64);

        LocalDateTime expiraEm = LocalDateTime.now().plusHours(24);

        resetTokenRepository.save(ResetToken.builder()
                .usuario(usuario)
                .token(token)
                .expiraEm(expiraEm)
                .build());

        auditLogService.registrar(usuario.getId(), "SOLICITAR_REDEFINICAO", "usuario",
                usuario.getId(), null);

        return new RedefinicaoResponse(
                token,
                expiraEm.toString(),
                "Token válido por 24 horas. Use POST /auth/redefinir-senha para alterar a senha."
        );
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaRequest request) {
        ResetToken rt = resetTokenRepository.findByTokenAndUsadoFalse(request.token())
                .orElseThrow(() -> new RuntimeException("Token inválido ou já utilizado"));

        if (rt.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        Usuario usuario = rt.getUsuario();
        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        usuario.setPrimeiroAcesso(false);
        usuarioRepository.save(usuario);

        rt.setUsado(true);
        resetTokenRepository.save(rt);

        auditLogService.registrar(usuario.getId(), "REDEFINIR_SENHA", "usuario",
                usuario.getId(), null);
    }
}