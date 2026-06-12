package br.gov.seed.questoes.repository;

import br.gov.seed.questoes.entity.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DisciplinaRepository extends JpaRepository<Disciplina, UUID> {
    Optional<Disciplina> findByNome(String nome);
}
