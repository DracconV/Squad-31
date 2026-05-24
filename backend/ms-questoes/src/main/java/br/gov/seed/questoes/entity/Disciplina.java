package br.gov.seed.questoes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "disciplina")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;
}
