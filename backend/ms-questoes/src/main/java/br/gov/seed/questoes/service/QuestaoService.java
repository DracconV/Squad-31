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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestaoService {

    private final QuestaoRepository questaoRepository;
    private final DisciplinaRepository disciplinaRepository;

    public Page<QuestaoResponse> listar(UUID disciplinaId, String dificuldade, String tipo, Pageable pageable) {
        return questaoRepository
                .filtrar(disciplinaId, dificuldade, tipo, pageable)
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
