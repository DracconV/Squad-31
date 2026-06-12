package br.gov.seed.relatorios.service;

import br.gov.seed.relatorios.dto.DesempenhoDTO.*;
import br.gov.seed.relatorios.entity.DesempenhoTurma;
import br.gov.seed.relatorios.exception.ResourceNotFoundException;
import br.gov.seed.relatorios.repository.DesempenhoAlunoRepository;
import br.gov.seed.relatorios.repository.DesempenhoTurmaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesempenhoTurmaService {

    private final DesempenhoTurmaRepository repository;
    private final DesempenhoAlunoRepository alunoRepository;

    public DesempenhoTurmaResponse obterDesempenhoTurma(UUID turmaId) {
        var desempenho = repository.findByTurmaId(turmaId)
            .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));
        return toResponse(desempenho);
    }

    @Transactional
    public void recalcularDesempenhoTurma(UUID turmaId) {
        log.info("Recalculando desempenho da turma: {}", turmaId);

        var alunosDesempenho = alunoRepository.findByTurmaId(turmaId);
        if (alunosDesempenho.isEmpty()) {
            log.warn("Turma sem alunos: {}", turmaId);
            return;
        }

        var mediaNotas = alunosDesempenho.stream()
            .map(d -> d.getNotaMedia().doubleValue())
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

        var taxaAcertos = alunosDesempenho.stream()
            .map(d -> d.getTaxaAcerto())
            .mapToDouble(Float::doubleValue)
            .average()
            .orElse(0.0f);

        var maiorNota = alunosDesempenho.stream()
            .map(d -> d.getNotaMedia())
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        var menorNota = alunosDesempenho.stream()
            .map(d -> d.getNotaMedia())
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        var desempenho = repository.findByTurmaId(turmaId)
            .orElse(DesempenhoTurma.builder()
                .turmaId(turmaId)
                .build());

        desempenho.setMediaTurma(BigDecimal.valueOf(mediaNotas));
        desempenho.setMaiorNota(maiorNota);
        desempenho.setMenorNota(menorNota);
        desempenho.setTaxaConclusao((float) taxaAcertos);
        desempenho.setAlunosAtivos(alunosDesempenho.size());
        desempenho.setTotalAlunos((int) alunosDesempenho.stream()
            .map(d -> d.getAlunoId())
            .distinct()
            .count());
        desempenho.setAtualizadoEm(LocalDateTime.now());

        repository.save(desempenho);
        log.info("Desempenho turma recalculado: media={}", mediaNotas);
    }

    private DesempenhoTurmaResponse toResponse(DesempenhoTurma desempenho) {
        return new DesempenhoTurmaResponse(
            desempenho.getId(),
            desempenho.getTurmaId(),
            desempenho.getMediaTurma(),
            desempenho.getMedianaTurma(),
            desempenho.getMaiorNota(),
            desempenho.getMenorNota(),
            desempenho.getTaxaConclusao(),
            desempenho.getAlunosAtivos(),
            desempenho.getTotalAlunos(),
            desempenho.getAtualizadoEm()
        );
    }
}

