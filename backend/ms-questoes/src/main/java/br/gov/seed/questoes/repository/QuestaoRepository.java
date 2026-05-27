package br.gov.seed.questoes.repository;

import br.gov.seed.questoes.entity.Questao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface QuestaoRepository extends JpaRepository<Questao, UUID> {

    Page<Questao> findByAtivaTrue(Pageable pageable);

    Page<Questao> findByDisciplina_IdAndAtivaTrue(UUID disciplinaId, Pageable pageable);

    // JOIN FETCH disciplina evita LazyInitializationException após mudança para FetchType.LAZY
    @Query(value = """
        SELECT q FROM Questao q JOIN FETCH q.disciplina
        WHERE q.ativa = true
          AND (:disciplinaId IS NULL OR q.disciplina.id = :disciplinaId)
          AND (:dificuldade IS NULL OR q.dificuldade = :dificuldade)
          AND (:tipo IS NULL OR q.tipo = :tipo)
          AND (:nivelEnsino IS NULL OR q.nivelEnsino = :nivelEnsino)
        """,
        countQuery = """
        SELECT count(q) FROM Questao q
        WHERE q.ativa = true
          AND (:disciplinaId IS NULL OR q.disciplina.id = :disciplinaId)
          AND (:dificuldade IS NULL OR q.dificuldade = :dificuldade)
          AND (:tipo IS NULL OR q.tipo = :tipo)
          AND (:nivelEnsino IS NULL OR q.nivelEnsino = :nivelEnsino)
        """)
    Page<Questao> filtrar(@Param("disciplinaId") UUID disciplinaId,
                          @Param("dificuldade") String dificuldade,
                          @Param("tipo") String tipo,
                          @Param("nivelEnsino") String nivelEnsino,
                          Pageable pageable);

    @Query(value = """
        SELECT q FROM Questao q JOIN FETCH q.disciplina
        WHERE q.ativa = true
          AND q.nivelEnsino IN :niveisPermitidos
          AND (:disciplinaId IS NULL OR q.disciplina.id = :disciplinaId)
          AND (:dificuldade IS NULL OR q.dificuldade = :dificuldade)
        """,
        countQuery = """
        SELECT count(q) FROM Questao q
        WHERE q.ativa = true
          AND q.nivelEnsino IN :niveisPermitidos
          AND (:disciplinaId IS NULL OR q.disciplina.id = :disciplinaId)
          AND (:dificuldade IS NULL OR q.dificuldade = :dificuldade)
        """)
    Page<Questao> filtrarPorNiveis(@Param("niveisPermitidos") List<String> niveisPermitidos,
                                   @Param("disciplinaId") UUID disciplinaId,
                                   @Param("dificuldade") String dificuldade,
                                   Pageable pageable);

    long countByAtivaTrue();

    @Query("SELECT q FROM Questao q JOIN FETCH q.disciplina WHERE q.criadoPor = :criadoPor AND q.ativa = true")
    Page<Questao> findByCriadoPorAndAtivaTrue(@Param("criadoPor") UUID criadoPor, Pageable pageable);
}
