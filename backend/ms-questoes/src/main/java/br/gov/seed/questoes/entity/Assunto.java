package br.gov.seed.questoes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "assunto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Assunto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Disciplina disciplina;
}
