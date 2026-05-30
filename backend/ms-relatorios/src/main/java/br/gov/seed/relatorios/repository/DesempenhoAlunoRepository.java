package br.gov.seed.relatorios.repository;

import br.gov.seed.relatorios.entity.DesempenhoAluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DesempenhoAlunoRepository extends JpaRepository<DesempenhoAluno, UUID> {

    Optional<DesempenhoAluno> findByAlunoIdAndDisciplina(UUID alunoId, String disciplina);

    List<DesempenhoAluno> findByAlunoId(UUID alunoId);

    List<DesempenhoAluno> findByTurmaId(UUID turmaId);

    Page<DesempenhoAluno> findByTurmaId(UUID turmaId, Pageable pageable);

    List<DesempenhoAluno> findByTurmaIdAndDisciplina(UUID turmaId, String disciplina);

    @Query("SELECT AVG(d.notaMedia) FROM DesempenhoAluno d WHERE d.notaMedia IS NOT NULL")
    Double findMediaGeral();

    @Query("SELECT AVG(d.notaMedia) FROM DesempenhoAluno d WHERE d.turmaId = :turmaId")
    Double findMediaTurma(UUID turmaId);

    @Query("SELECT AVG(d.taxaAcerto) FROM DesempenhoAluno d WHERE d.turmaId = :turmaId")
    Double findTaxaAcertoMediaTurma(UUID turmaId);

    @Query("SELECT d FROM DesempenhoAluno d WHERE d.turmaId = :turmaId AND d.notaMedia < :limiar ORDER BY d.notaMedia ASC")
    List<DesempenhoAluno> findAlunosComBaixoDesempenho(UUID turmaId, java.math.BigDecimal limiar);
}

