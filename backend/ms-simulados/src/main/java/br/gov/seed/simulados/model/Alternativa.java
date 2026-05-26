package br.gov.seed.simulados.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

/**
 * Leitura da tabela alternativa (V3__criar_banco_questoes.sql).
 * Usada apenas para checar se a resposta do aluno está correta ao finalizar.
 * Entidade read-only — ms-simulados não altera alternativas.
 */
@Getter
@Entity
@Table(name = "alternativa")
public class Alternativa {

    @Id
    private UUID id;

    private boolean correta;
}
