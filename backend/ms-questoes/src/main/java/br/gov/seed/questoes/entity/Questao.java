package br.gov.seed.questoes.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questao")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false, length = 10)
    private String dificuldade;

    @Column(name = "tipo_uso", nullable = false, length = 10)
    private String tipoUso;

    @Column(name = "nivel_ensino", nullable = false, length = 20)
    private String nivelEnsino = "MEDIO";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Disciplina disciplina;

    @Column(name = "criado_por", nullable = false)
    private UUID criadoPor;

    @Column(nullable = false)
    private boolean ativa = true;

    @OneToMany(mappedBy = "questao", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<Alternativa> alternativas;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}
