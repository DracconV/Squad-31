package br.gov.seed.questoes.repository;

import br.gov.seed.questoes.entity.Questao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface QuestaoRepository extends JpaRepository<Questao, UUID> {

    Page<Questao> findByAtivaTrue(Pageable pageable);

    Page<Questao> findByDisciplina_IdAndAtivaTrue(UUID disciplinaId, Pageable pageable);

    @Query("SELECT q FROM Questao q WHERE q.ativa = true AND (:disciplinaId IS NULL OR q.disciplina.id = :disciplinaId) AND (:dificuldade IS NULL OR q.dificuldade = :dificuldade) AND (:tipo IS NULL OR q.tipo = :tipo)")
    Page<Questao> filtrar(@Param("disciplinaId") UUID disciplinaId,
                          @Param("dificuldade") String dificuldade,
                          @Param("tipo") String tipo,
                          Pageable pageable);

    long countByAtivaTrue();
}
