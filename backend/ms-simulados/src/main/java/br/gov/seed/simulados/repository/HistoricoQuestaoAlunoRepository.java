package br.gov.seed.simulados.repository;

import br.gov.seed.simulados.model.HistoricoQuestaoAluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HistoricoQuestaoAlunoRepository extends JpaRepository<HistoricoQuestaoAluno, UUID> {
}
