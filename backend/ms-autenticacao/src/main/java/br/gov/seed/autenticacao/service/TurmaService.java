package br.gov.seed.autenticacao.service;

import br.gov.seed.autenticacao.dto.TurmaDTO;
import br.gov.seed.autenticacao.entity.AlunoTurma;
import br.gov.seed.autenticacao.entity.Instituicao;
import br.gov.seed.autenticacao.entity.Turma;
import br.gov.seed.autenticacao.repository.AlunoTurmaRepository;
import br.gov.seed.autenticacao.repository.InstituicaoRepository;
import br.gov.seed.autenticacao.repository.TurmaRepository;
import br.gov.seed.autenticacao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlunoTurmaRepository alunoTurmaRepository;

    @Transactional
    public TurmaDTO.Response criar(TurmaDTO.CriarRequest request, UUID professorId) {
        Instituicao instituicao = instituicaoRepository.findById(request.instituicaoId())
                .orElseThrow(() -> new IllegalArgumentException("Instituicao nao encontrada: " + request.instituicaoId()));

        Turma turma = Turma.builder()
                .nome(request.nome())
                .ano(request.ano())
                .modalidade(request.modalidade())
                .instituicao(instituicao)
                .professorId(professorId)
                .ativo(true)
                .build();

        return TurmaDTO.Response.from(turmaRepository.save(turma));
    }

    public List<TurmaDTO.Response> listarMinhas(UUID professorId) {
        return turmaRepository.findByProfessorIdAndAtivoTrueOrderByNomeAsc(professorId).stream()
                .map(TurmaDTO.Response::from)
                .toList();
    }

    public List<TurmaDTO.Response> listarTodas() {
        return turmaRepository.findByAtivoTrueOrderByNomeAsc().stream()
                .map(TurmaDTO.Response::from)
                .toList();
    }

    public List<TurmaDTO.Response> listarPorInstituicao(UUID instituicaoId) {
        return turmaRepository.findByInstituicaoIdAndAtivoTrueOrderByNomeAsc(instituicaoId).stream()
                .map(TurmaDTO.Response::from)
                .toList();
    }

    @Transactional
    public TurmaDTO.Response atualizar(UUID id, TurmaDTO.AtualizarRequest request) {
        Turma turma = turmaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma nao encontrada: " + id));
        if (request.nome() != null && !request.nome().isBlank()) {
            turma.setNome(request.nome());
        }
        if (request.ano() != null && request.ano() > 0) {
            turma.setAno(request.ano());
        }
        if (request.modalidade() != null && !request.modalidade().isBlank()) {
            turma.setModalidade(request.modalidade());
        }
        return TurmaDTO.Response.from(turmaRepository.save(turma));
    }

    @Transactional
    public void adicionarAluno(UUID turmaId, UUID alunoId) {
        if (!turmaRepository.existsById(turmaId)) {
            throw new IllegalArgumentException("Turma nao encontrada: " + turmaId);
        }
        if (!usuarioRepository.existsById(alunoId)) {
            throw new IllegalArgumentException("Aluno nao encontrado: " + alunoId);
        }
        if (alunoTurmaRepository.existsByAlunoIdAndTurmaId(alunoId, turmaId)) {
            throw new IllegalArgumentException("Aluno ja esta na turma");
        }

        AlunoTurma at = new AlunoTurma();
        at.setAlunoId(alunoId);
        at.setTurmaId(turmaId);
        alunoTurmaRepository.save(at);
    }

    @Transactional
    public void removerAluno(UUID turmaId, UUID alunoId) {
        AlunoTurma.AlunoTurmaId id = new AlunoTurma.AlunoTurmaId(alunoId, turmaId);
        if (!alunoTurmaRepository.existsById(id)) {
            throw new IllegalArgumentException("Aluno nao esta na turma");
        }
        alunoTurmaRepository.deleteById(id);
    }

    public List<TurmaDTO.AlunoResponse> listarAlunos(UUID turmaId) {
        if (!turmaRepository.existsById(turmaId)) {
            throw new IllegalArgumentException("Turma nao encontrada: " + turmaId);
        }
        return alunoTurmaRepository.findByTurmaIdWithAluno(turmaId).stream()
                .map(TurmaDTO.AlunoResponse::from)
                .toList();
    }
}
