package br.gov.seed.simulados.repository;

import br.gov.seed.simulados.model.SimuladoQuestao;
import br.gov.seed.simulados.model.SimuladoQuestaoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SimuladoQuestaoRepository extends JpaRepository<SimuladoQuestao, SimuladoQuestaoId> {

    List<SimuladoQuestao> findByIdSimuladoIdOrderByOrdem(UUID simuladoId);
}
