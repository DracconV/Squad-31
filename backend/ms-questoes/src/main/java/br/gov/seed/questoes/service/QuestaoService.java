package br.gov.seed.questoes.service;

import br.gov.seed.questoes.dto.DisciplinaDto;
import br.gov.seed.questoes.dto.QuestaoResponse;
import br.gov.seed.questoes.entity.Questao;
import br.gov.seed.questoes.repository.DisciplinaRepository;
import br.gov.seed.questoes.repository.QuestaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestaoService {

    private final QuestaoRepository questaoRepository;
    private final DisciplinaRepository disciplinaRepository;

    // Quais níveis cada modalidade de turma pode acessar
    private static final Map<String, List<String>> NIVEIS_POR_MODALIDADE = Map.of(
        "MEDIO",             List.of("MEDIO"),
        "EJA",               List.of("FUNDAMENTAL", "MEDIO"),
        "PROFISSIONALIZANTE", List.of("MEDIO", "PROFISSIONALIZANTE")
    );

    public Page<QuestaoResponse> listar(UUID disciplinaId, String dificuldade,
                                         String tipo, String nivelEnsino,
                                         String modalidadeTurma, Pageable pageable) {
        // Se informou modalidade de turma, usa os níveis permitidos para ela
        if (modalidadeTurma != null && !modalidadeTurma.isBlank()) {
            List<String> niveisPermitidos = NIVEIS_POR_MODALIDADE.getOrDefault(
                modalidadeTurma.toUpperCase(), List.of("MEDIO")
            );
            return questaoRepository
                .filtrarPorNiveis(niveisPermitidos, disciplinaId, dificuldade, pageable)
                .map(QuestaoResponse::from);
        }

        return questaoRepository
            .filtrar(disciplinaId, dificuldade, tipo, nivelEnsino, pageable)
            .map(QuestaoResponse::from);
    }

    public QuestaoResponse buscarPorId(UUID id) {
        Questao q = questaoRepository.findById(id)
                .filter(Questao::isAtiva)
                .orElseThrow(() -> new RuntimeException("Questão não encontrada: " + id));
        return QuestaoResponse.from(q);
    }

    public List<DisciplinaDto> listarDisciplinas() {
        return disciplinaRepository.findAll().stream()
                .map(d -> new DisciplinaDto(d.getId(), d.getNome()))
                .collect(Collectors.toList());
    }

    public long total() {
        return questaoRepository.countByAtivaTrue();
    }
}
