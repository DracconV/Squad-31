package br.gov.seed.questoes.repository;

import br.gov.seed.questoes.entity.QuestaoFavorita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestaoFavoritaRepository
        extends JpaRepository<QuestaoFavorita, QuestaoFavorita.PK> {

    List<QuestaoFavorita> findByAlunoIdOrderByCriadoEmDesc(UUID alunoId);

    boolean existsByAlunoIdAndQuestaoId(UUID alunoId, UUID questaoId);

    void deleteByAlunoIdAndQuestaoId(UUID alunoId, UUID questaoId);
}
