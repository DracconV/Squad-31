package br.gov.seed.simulados.repository;

import br.gov.seed.simulados.model.Alternativa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlternativaRepository extends JpaRepository<Alternativa, UUID> {

    java.util.Optional<Alternativa> findByQuestaoIdAndCorretaTrue(UUID questaoId);
}
