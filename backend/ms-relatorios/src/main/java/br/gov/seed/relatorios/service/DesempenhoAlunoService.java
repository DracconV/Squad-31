package br.gov.seed.relatorios.service;

import br.gov.seed.relatorios.dto.DesempenhoDTO.*;
import br.gov.seed.relatorios.entity.DesempenhoAluno;
import br.gov.seed.relatorios.exception.ResourceNotFoundException;
import br.gov.seed.relatorios.repository.DesempenhoAlunoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesempenhoAlunoService {

    private final DesempenhoAlunoRepository repository;

    public DesempenhoAlunoResponse obterDesempenho(UUID alunoId, String disciplina) {
        var desempenho = repository.findByAlunoIdAndDisciplina(alunoId, disciplina)
            .orElseThrow(() -> new ResourceNotFoundException("Desempenho não encontrado"));
        return toResponse(desempenho);
    }

    public List<DesempenhoAlunoResponse> obterHistoricoAluno(UUID alunoId) {
        return repository.findByAlunoId(alunoId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void atualizarDesempenho(UUID alunoId, UUID turmaId, String disciplina,
                                   Integer acertos, Integer total) {
        log.info("Atualizando desempenho: alunoId={}, disciplina={}, acertos={}/{}",
            alunoId, disciplina, acertos, total);

        var desempenho = repository.findByAlunoIdAndDisciplina(alunoId, disciplina)
            .orElse(DesempenhoAluno.builder()
                .alunoId(alunoId)
                .turmaId(turmaId)
                .disciplina(disciplina)
                .questoesAcertadas(0)
                .questoesTotal(0)
                .build());

        desempenho.setQuestoesAcertadas(desempenho.getQuestoesAcertadas() + acertos);
        desempenho.setQuestoesTotal(desempenho.getQuestoesTotal() + total);
        desempenho.calcularTaxaAcerto();
        desempenho.calcularNotaMedia();
        desempenho.setAtualizadoEm(LocalDateTime.now());

        repository.save(desempenho);
        log.info("Desempenho atualizado: taxaAcerto={}%", desempenho.getTaxaAcerto());
    }

    public List<DesempenhoAlunoResponse> obterAlunosComBaixoDesempenho(UUID turmaId) {
        var limiar = BigDecimal.valueOf(6.0);
        return repository.findAlunosComBaixoDesempenho(turmaId, limiar)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private DesempenhoAlunoResponse toResponse(DesempenhoAluno desempenho) {
        return new DesempenhoAlunoResponse(
            desempenho.getId(),
            desempenho.getAlunoId(),
            desempenho.getTurmaId(),
            desempenho.getDisciplina(),
            desempenho.getNotaMedia(),
            desempenho.getQuestoesAcertadas(),
            desempenho.getQuestoesTotal(),
            desempenho.getTaxaAcerto(),
            desempenho.getAtualizadoEm()
        );
    }
}

