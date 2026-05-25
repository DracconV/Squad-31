package org.example.mssimulados.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessaoSimulado implements Serializable {

private Long simuladoId;
private LocalDateTime iniciadoEm;
private int questaoAtual = 0;
private Map<Integer, String> respostas = new HashMap<>();
}