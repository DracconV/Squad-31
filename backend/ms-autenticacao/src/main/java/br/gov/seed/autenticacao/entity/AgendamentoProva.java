package br.gov.seed.autenticacao.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "agendamento_prova")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AgendamentoProva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aluno_id", nullable = false)
    private UUID alunoId;

    @Column(name = "slot_id", nullable = false)
    private UUID slotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", insertable = false, updatable = false)
    private Usuario aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", insertable = false, updatable = false)
    private SlotProvaPratica slot;
}
