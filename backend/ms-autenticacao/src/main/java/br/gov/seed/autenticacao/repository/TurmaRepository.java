package br.gov.seed.autenticacao.repository;

import br.gov.seed.autenticacao.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TurmaRepository extends JpaRepository<Turma, UUID> {
    List<Turma> findByInstituicaoIdAndAtivoTrueOrderByNomeAsc(UUID instituicaoId);
    List<Turma> findByAtivoTrueOrderByNomeAsc();
    List<Turma> findByProfessorIdAndAtivoTrueOrderByNomeAsc(UUID professorId);
}
