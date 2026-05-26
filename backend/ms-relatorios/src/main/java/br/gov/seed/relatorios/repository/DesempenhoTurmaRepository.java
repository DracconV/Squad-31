package br.gov.seed.relatorios.repository;

import br.gov.seed.relatorios.entity.DesempenhoTurma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DesempenhoTurmaRepository extends JpaRepository<DesempenhoTurma, UUID> {

    Optional<DesempenhoTurma> findByTurmaId(UUID turmaId);
}

