package br.gov.seed.autenticacao.repository;

import br.gov.seed.autenticacao.entity.AlunoTurma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface AlunoTurmaRepository extends JpaRepository<AlunoTurma, AlunoTurma.AlunoTurmaId> {

    @Query("SELECT at FROM AlunoTurma at JOIN FETCH at.aluno WHERE at.turmaId = :turmaId")
    List<AlunoTurma> findByTurmaIdWithAluno(@Param("turmaId") UUID turmaId);

    boolean existsByAlunoIdAndTurmaId(UUID alunoId, UUID turmaId);
}
