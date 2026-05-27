package br.gov.seed.simulados.repository;

import br.gov.seed.simulados.model.TentativaSimulado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TentativaSimuladoRepository extends JpaRepository<TentativaSimulado, UUID> {

    List<TentativaSimulado> findByAlunoIdOrderByIniciadoEmDesc(UUID alunoId);

    Optional<TentativaSimulado> findTopByAlunoIdAndSimuladoIdOrderByIniciadoEmDesc(UUID alunoId, UUID simuladoId);
}
