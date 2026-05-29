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
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        return usuarioRepository.findByInstituicao_Id(instituicaoId).stream()
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

    /**
     * Importa usuários em lote a partir de arquivo CSV.
     *
     * Formato esperado (primeira linha = cabeçalho, ignorada):
     *   nome,matricula,cpf,email,perfil,senhaTemporaria,instituicaoId
     *
     * Campos opcionais: cpf, email, instituicaoId.
     * Perfis válidos: ALUNO_EM, ALUNO_EJA, ALUNO_PROF, PROFESSOR, ADMIN_ESCOLA, ADMIN_SEED.
     */
    @Transactional
    public UsuarioDTO.ImportacaoResult importarCsv(MultipartFile arquivo) throws IOException {
        List<UsuarioDTO.ImportacaoItem> detalhes = new ArrayList<>();
        int linhaAtual = 0;
        int importados = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {

            String cabecalho = reader.readLine(); // ignora cabeçalho
            if (cabecalho == null) {
                return new UsuarioDTO.ImportacaoResult(0, 0, 0, List.of());
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                linhaAtual++;

                try {
                    String[] cols = line.split(",", -1);
                    if (cols.length < 6) {
                        detalhes.add(new UsuarioDTO.ImportacaoItem(
                                linhaAtual, "", "erro", "Linha com colunas insuficientes (mín. 6)"));
                        continue;
                    }

                    String nome        = cols[0].trim();
                    String matricula   = cols[1].trim();
                    String cpf         = cols[2].trim().isEmpty() ? null : cols[2].trim();
                    String email       = cols[3].trim().isEmpty() ? null : cols[3].trim();
                    String perfilStr   = cols[4].trim();
                    String senha       = cols[5].trim();
                    String instIdStr   = cols.length > 6 ? cols[6].trim() : null;

                    UUID instId = (instIdStr != null && !instIdStr.isEmpty())
                            ? UUID.fromString(instIdStr) : null;

                    Usuario.Perfil perfil = Usuario.Perfil.valueOf(perfilStr);

                    criar(new UsuarioDTO.CriarRequest(nome, matricula, cpf, email, senha, perfil, instId));
                    importados++;
                    detalhes.add(new UsuarioDTO.ImportacaoItem(linhaAtual, matricula, "ok", "Importado com sucesso"));

                } catch (Exception e) {
                    String[] cols = line.split(",", -1);
                    String mat = cols.length > 1 ? cols[1].trim() : "";
                    detalhes.add(new UsuarioDTO.ImportacaoItem(linhaAtual, mat, "erro", e.getMessage()));
                }
            }
        }

        return new UsuarioDTO.ImportacaoResult(linhaAtual, importados, linhaAtual - importados, detalhes);
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
