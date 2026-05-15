package br.gov.seed.autenticacao.service;

import br.gov.seed.autenticacao.dto.AuthDTO.*;
import br.gov.seed.autenticacao.entity.Usuario;
import br.gov.seed.autenticacao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.matricula(), request.senha())
            );
        } catch (Exception e) {
            throw e;
        }

        Usuario usuario = usuarioRepository.findByMatricula(request.matricula())
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        String token = jwtService.gerarToken(usuario);

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
    }
}