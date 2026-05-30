package br.gov.seed.questoes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/** Questão marcada para revisão por um aluno (chave composta aluno+questão). */
@Entity
@Table(name = "questao_favorita")
@IdClass(QuestaoFavorita.PK.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuestaoFavorita {

    @Id
    @Column(name = "aluno_id", nullable = false)
    private UUID alunoId;

    @Id
    @Column(name = "questao_id", nullable = false)
    private UUID questaoId;

    @Builder.Default
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private UUID alunoId;
        private UUID questaoId;
    }
}
