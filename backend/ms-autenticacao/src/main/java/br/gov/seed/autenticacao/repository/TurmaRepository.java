package br.gov.seed.autenticacao.repository;

import br.gov.seed.autenticacao.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface TurmaRepository extends JpaRepository<Turma, UUID> {

    // JOIN FETCH da instituição evita N+1 no Response.from() (instituicao é LAZY)
    @Query("SELECT t FROM Turma t JOIN FETCH t.instituicao WHERE t.instituicao.id = :instituicaoId AND t.ativo = true ORDER BY t.nome ASC")
    List<Turma> findByInstituicaoIdAndAtivoTrueOrderByNomeAsc(@Param("instituicaoId") UUID instituicaoId);

    @Query("SELECT t FROM Turma t JOIN FETCH t.instituicao WHERE t.ativo = true ORDER BY t.nome ASC")
    List<Turma> findByAtivoTrueOrderByNomeAsc();

    @Query("SELECT t FROM Turma t JOIN FETCH t.instituicao WHERE t.professorId = :professorId AND t.ativo = true ORDER BY t.nome ASC")
    List<Turma> findByProfessorIdAndAtivoTrueOrderByNomeAsc(@Param("professorId") UUID professorId);
}
