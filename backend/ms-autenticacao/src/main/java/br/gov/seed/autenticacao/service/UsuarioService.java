package br.gov.seed.autenticacao.service;

import br.gov.seed.autenticacao.dto.UsuarioDTO;
import br.gov.seed.autenticacao.entity.Instituicao;
import br.gov.seed.autenticacao.entity.Usuario;
import br.gov.seed.autenticacao.repository.InstituicaoRepository;
import br.gov.seed.autenticacao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional
    public UsuarioDTO.Response criar(UsuarioDTO.CriarRequest request) {
        if (usuarioRepository.existsByMatricula(request.matricula())) {
            throw new IllegalArgumentException("Matricula ja cadastrada: " + request.matricula());
        }
        if (request.cpf() != null && !request.cpf().isBlank() && usuarioRepository.existsByCpf(request.cpf())) {
            throw new IllegalArgumentException("CPF ja cadastrado");
        }

        Instituicao instituicao = null;
        if (request.instituicaoId() != null) {
            instituicao = instituicaoRepository.findById(request.instituicaoId())
                    .orElseThrow(() -> new IllegalArgumentException("Instituicao nao encontrada: " + request.instituicaoId()));
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .matricula(request.matricula())
                .cpf(request.cpf())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senhaTemporaria()))
                .perfil(request.perfil())
                .instituicao(instituicao)
                .primeiroAcesso(true)
                .ativo(true)
                .build();

        UsuarioDTO.Response resp = UsuarioDTO.Response.from(usuarioRepository.save(usuario));
        auditLogService.registrar(null, "CRIAR_USUARIO", "usuario", resp.id(),
                java.util.Map.of("matricula", request.matricula(), "perfil", request.perfil().name()));
        return resp;
    }

    public List<UsuarioDTO.Response> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioDTO.Response::from)
                .toList();
    }

    public List<UsuarioDTO.Response> listarPorInstituicao(UUID instituicaoId) {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getInstituicao() != null && u.getInstituicao().getId().equals(instituicaoId))
                .map(UsuarioDTO.Response::from)
                .toList();
    }

    public UsuarioDTO.Response buscarPorId(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado: " + id));
        return UsuarioDTO.Response.from(usuario);
    }

    @Transactional
    public UsuarioDTO.Response atualizar(UUID id, UsuarioDTO.AtualizarRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado: " + id));
        if (request.nome() != null && !request.nome().isBlank()) {
            usuario.setNome(request.nome());
        }
        if (request.email() != null) {
            usuario.setEmail(request.email());
        }
        if (request.cpf() != null && !request.cpf().isBlank()) {
            if (!request.cpf().equals(usuario.getCpf()) && usuarioRepository.existsByCpf(request.cpf())) {
                throw new IllegalArgumentException("CPF ja cadastrado por outro usuario");
            }
            usuario.setCpf(request.cpf());
        }
        if (request.instituicaoId() != null) {
            Instituicao inst = instituicaoRepository.findById(request.instituicaoId())
                    .orElseThrow(() -> new IllegalArgumentException("Instituicao nao encontrada: " + request.instituicaoId()));
            usuario.setInstituicao(inst);
        }
        usuario.setAtualizadoEm(java.time.LocalDateTime.now());
        UsuarioDTO.Response resp = UsuarioDTO.Response.from(usuarioRepository.save(usuario));
        auditLogService.registrar(id, "ATUALIZAR_USUARIO", "usuario", id,
                java.util.Map.of("nome", usuario.getNome()));
        return resp;
    }

    @Transactional
    public UsuarioDTO.Response desativar(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado: " + id));
        usuario.setAtivo(false);
        UsuarioDTO.Response resp = UsuarioDTO.Response.from(usuarioRepository.save(usuario));
        auditLogService.registrar(id, "DESATIVAR_USUARIO", "usuario", id, null);
        return resp;
    }

    @Transactional
    public UsuarioDTO.Response reativar(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado: " + id));
        usuario.setAtivo(true);
        UsuarioDTO.Response resp = UsuarioDTO.Response.from(usuarioRepository.save(usuario));
        auditLogService.registrar(id, "REATIVAR_USUARIO", "usuario", id, null);
        return resp;
    }
}
