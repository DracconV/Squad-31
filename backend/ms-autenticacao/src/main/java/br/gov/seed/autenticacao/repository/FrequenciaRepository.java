package br.gov.seed.autenticacao.repository;

import br.gov.seed.autenticacao.entity.Frequencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FrequenciaRepository extends JpaRepository<Frequencia, UUID> {

    List<Frequencia> findByAlunoIdOrderByDisciplinaAsc(UUID alunoId);

    List<Frequencia> findByTurmaId(UUID turmaId);

    Optional<Frequencia> findByAlunoIdAndDisciplina(UUID alunoId, String disciplina);
}
