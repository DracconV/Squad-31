package br.gov.seed.relatorios.repository;

import br.gov.seed.relatorios.entity.DiagnosticoAluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiagnosticoAlunoRepository extends JpaRepository<DiagnosticoAluno, UUID> {

    /** Último diagnóstico por disciplina de um aluno. */
    Optional<DiagnosticoAluno> findTopByAlunoIdAndDisciplinaIdOrderByGeradoEmDesc(UUID alunoId, UUID disciplinaId);

    /** Todos os diagnósticos de um aluno, do mais recente para o mais antigo. */
    List<DiagnosticoAluno> findByAlunoIdOrderByGeradoEmDesc(UUID alunoId);
}
