package br.gov.seed.simulados.repository;

import br.gov.seed.simulados.model.Simulado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SimuladoRepository extends JpaRepository<Simulado, UUID> {

    /** Simulados disponíveis: dataFim no futuro ou sem dataFim definida. */
    @Query("SELECT s FROM Simulado s WHERE s.dataFim IS NULL OR s.dataFim > :agora ORDER BY s.criadoEm DESC")
    List<Simulado> findDisponiveis(LocalDateTime agora);

    List<Simulado> findByTurmaIdOrderByCriadoEmDesc(UUID turmaId);

    List<Simulado> findByProfessorIdOrderByCriadoEmDesc(UUID professorId);
}
