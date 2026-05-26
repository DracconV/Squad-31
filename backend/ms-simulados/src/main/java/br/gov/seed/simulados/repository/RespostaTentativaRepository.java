package br.gov.seed.simulados.repository;

import br.gov.seed.simulados.model.RespostaTentativa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RespostaTentativaRepository extends JpaRepository<RespostaTentativa, UUID> {

    List<RespostaTentativa> findByTentativaId(UUID tentativaId);
}
