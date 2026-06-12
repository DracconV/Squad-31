package br.gov.seed.autenticacao.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "slot_prova_pratica")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SlotProvaPratica {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "modulo_id", nullable = false)
    private UUID moduloId;

    @Column(nullable = false)
    private LocalDateTime data;

    @Column(nullable = false)
    private String local;

    @Column(name = "vagas_totais", nullable = false)
    private Integer vagasTotais;

    @Column(name = "vagas_ocupadas", nullable = false)
    private Integer vagasOcupadas = 0;
}
