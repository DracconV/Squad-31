package br.gov.seed.autenticacao.repository;

import br.gov.seed.autenticacao.entity.AgendamentoProva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface AgendamentoProvaRepository extends JpaRepository<AgendamentoProva, UUID> {

    @Query("SELECT a FROM AgendamentoProva a JOIN FETCH a.slot WHERE a.alunoId = :alunoId ORDER BY a.slot.data ASC")
    List<AgendamentoProva> findByAlunoIdWithSlot(@Param("alunoId") UUID alunoId);

    boolean existsByAlunoIdAndSlotId(UUID alunoId, UUID slotId);
}
