package br.gov.seed.autenticacao.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "aluno_turma")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@IdClass(AlunoTurma.AlunoTurmaId.class)
public class AlunoTurma {

    @Id
    @Column(name = "aluno_id")
    private UUID alunoId;

    @Id
    @Column(name = "turma_id")
    private UUID turmaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", insertable = false, updatable = false)
    private Usuario aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", insertable = false, updatable = false)
    private Turma turma;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class AlunoTurmaId implements Serializable {
        private UUID alunoId;
        private UUID turmaId;
    }
}
