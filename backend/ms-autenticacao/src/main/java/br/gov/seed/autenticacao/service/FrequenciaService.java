package br.gov.seed.autenticacao.service;

import br.gov.seed.autenticacao.dto.FrequenciaDTO;
import br.gov.seed.autenticacao.entity.AlunoTurma;
import br.gov.seed.autenticacao.entity.Frequencia;
import br.gov.seed.autenticacao.repository.AlunoTurmaRepository;
import br.gov.seed.autenticacao.repository.FrequenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FrequenciaService {

    /** Limite mínimo de presença para aprovação (%). */
    private static final int LIMITE_PRESENCA = 75;

    private final FrequenciaRepository frequenciaRepo;
    private final AlunoTurmaRepository alunoTurmaRepo;

    /** Registra (ou atualiza) a frequência de um aluno numa disciplina. */
    @Transactional
    public FrequenciaDTO.ResumoItem registrar(FrequenciaDTO.RegistrarRequest req) {
        if (req.faltas() > req.totalAulas()) {
            throw new IllegalArgumentException("Faltas não podem exceder o total de aulas");
        }
        Frequencia f = frequenciaRepo.findByAlunoIdAndDisciplina(req.alunoId(), req.disciplina())
                .orElseGet(() -> Frequencia.builder()
                        .alunoId(req.alunoId())
                        .disciplina(req.disciplina())
                        .build());
        f.setTurmaId(req.turmaId());
        f.setTotalAulas(req.totalAulas());
        f.setFaltas(req.faltas());
        f.setAtualizadoEm(LocalDateTime.now());
        Frequencia salvo = frequenciaRepo.save(f);
        return toResumoItem(salvo);
    }

    /** Resumo de frequência do aluno, uma linha por disciplina. */
    public List<FrequenciaDTO.ResumoItem> resumoAluno(UUID alunoId) {
        return frequenciaRepo.findByAlunoIdOrderByDisciplinaAsc(alunoId).stream()
                .map(this::toResumoItem)
                .collect(Collectors.toList());
    }

    /** Visão agregada da turma: presença média e situação por aluno. */
    public FrequenciaDTO.TurmaResponse turma(UUID turmaId) {
        List<AlunoTurma> matriculas = alunoTurmaRepo.findByTurmaIdWithAluno(turmaId);

        // alunoId -> [somaFaltas, somaAulas]
        Map<UUID, int[]> agregadoPorAluno = new java.util.HashMap<>();
        for (Frequencia f : frequenciaRepo.findByTurmaId(turmaId)) {
            int[] acc = agregadoPorAluno.computeIfAbsent(f.getAlunoId(), k -> new int[2]);
            acc[0] += (f.getFaltas() == null ? 0 : f.getFaltas());
            acc[1] += (f.getTotalAulas() == null ? 0 : f.getTotalAulas());
        }

        List<FrequenciaDTO.TurmaAlunoItem> alunos = new ArrayList<>();
        double somaPresenca = 0;
        int emAtencao = 0;

        for (AlunoTurma m : matriculas) {
            int[] acc = agregadoPorAluno.getOrDefault(m.getAlunoId(), new int[]{0, 0});
            int faltas = acc[0];
            int aulas = acc[1];
            double presenca = aulas > 0 ? Math.round((aulas - faltas) * 10000.0 / aulas) / 100.0 : 100.0;
            String status = statusPresenca(presenca);
            if (!"OK".equals(status)) emAtencao++;
            somaPresenca += presenca;

            String nome = m.getAluno() != null ? m.getAluno().getNome() : "—";
            String matricula = m.getAluno() != null ? m.getAluno().getMatricula() : "—";
            alunos.add(new FrequenciaDTO.TurmaAlunoItem(m.getAlunoId(), nome, matricula, faltas, presenca, status));
        }

        double presencaMedia = alunos.isEmpty() ? 0.0
                : Math.round(somaPresenca / alunos.size() * 100.0) / 100.0;

        return new FrequenciaDTO.TurmaResponse(
                turmaId, presencaMedia, alunos.size(), emAtencao, alunos, LocalDateTime.now());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private FrequenciaDTO.ResumoItem toResumoItem(Frequencia f) {
        return new FrequenciaDTO.ResumoItem(
                f.getDisciplina(),
                f.getTotalAulas() == null ? 0 : f.getTotalAulas(),
                f.getFaltas() == null ? 0 : f.getFaltas(),
                f.getPresenca(),
                LIMITE_PRESENCA,
                f.getAtualizadoEm());
    }

    /** OK ≥ 85% · ACOMPANHAR 75–85% · CRITICO < 75%. */
    private String statusPresenca(double presenca) {
        if (presenca >= 85) return "OK";
        if (presenca >= LIMITE_PRESENCA) return "ACOMPANHAR";
        return "CRITICO";
    }
}
