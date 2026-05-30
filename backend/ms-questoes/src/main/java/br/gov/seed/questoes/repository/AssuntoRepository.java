package br.gov.seed.questoes.repository;

import br.gov.seed.questoes.entity.Assunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssuntoRepository extends JpaRepository<Assunto, UUID> {

    List<Assunto> findByDisciplinaIdOrderByNomeAsc(UUID disciplinaId);

    List<Assunto> findAllByOrderByNomeAsc();

    boolean existsByNomeAndDisciplinaId(String nome, UUID disciplinaId);
}
