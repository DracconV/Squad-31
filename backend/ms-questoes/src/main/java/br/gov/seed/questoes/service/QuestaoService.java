package br.gov.seed.questoes.service;

import br.gov.seed.questoes.dto.AssuntoDto;
import br.gov.seed.questoes.dto.CriarQuestaoRequest;
import br.gov.seed.questoes.dto.DisciplinaDto;
import br.gov.seed.questoes.dto.QuestaoResponse;
import br.gov.seed.questoes.entity.Alternativa;
import br.gov.seed.questoes.entity.Assunto;
import br.gov.seed.questoes.entity.Disciplina;
import br.gov.seed.questoes.entity.Questao;
import br.gov.seed.questoes.entity.QuestaoFavorita;
import br.gov.seed.questoes.repository.AssuntoRepository;
import br.gov.seed.questoes.repository.DisciplinaRepository;
import br.gov.seed.questoes.repository.QuestaoFavoritaRepository;
import br.gov.seed.questoes.repository.QuestaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestaoService {

    private final QuestaoRepository questaoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final AssuntoRepository assuntoRepository;
    private final QuestaoFavoritaRepository favoritaRepository;

    // Quais níveis cada modalidade de turma pode acessar
    private static final Map<String, List<String>> NIVEIS_POR_MODALIDADE = Map.of(
        "MEDIO",             List.of("MEDIO"),
        "EJA",               List.of("FUNDAMENTAL", "MEDIO"),
        "PROFISSIONALIZANTE", List.of("MEDIO", "PROFISSIONALIZANTE")
    );

    public Page<QuestaoResponse> listar(UUID disciplinaId, String dificuldade,
                                         String tipo, String nivelEnsino,
                                         String modalidadeTurma, Pageable pageable,
                                         boolean incluirGabarito) {
        if (modalidadeTurma != null && !modalidadeTurma.isBlank()) {
            List<String> niveisPermitidos = NIVEIS_POR_MODALIDADE.getOrDefault(
                modalidadeTurma.toUpperCase(), List.of("MEDIO")
            );
            return questaoRepository
                .filtrarPorNiveis(niveisPermitidos, disciplinaId, dificuldade, pageable)
                .map(q -> QuestaoResponse.from(q, incluirGabarito));
        }

        return questaoRepository
            .filtrar(disciplinaId, dificuldade, tipo, nivelEnsino, pageable)
            .map(q -> QuestaoResponse.from(q, incluirGabarito));
    }

    public QuestaoResponse buscarPorId(UUID id, boolean incluirGabarito) {
        Questao q = questaoRepository.findById(id)
                .filter(Questao::isAtiva)
                .orElseThrow(() -> new RuntimeException("Questão não encontrada: " + id));
        return QuestaoResponse.from(q, incluirGabarito);
    }

    @Transactional
    public QuestaoResponse criar(CriarQuestaoRequest request, UUID professorId) {
        Disciplina disciplina = disciplinaRepository.findById(request.disciplinaId())
                .orElseThrow(() -> new IllegalArgumentException("Disciplina não encontrada: " + request.disciplinaId()));

        long corretas = request.alternativas().stream().filter(CriarQuestaoRequest.AlternativaRequest::correta).count();
        if (corretas != 1) {
            throw new IllegalArgumentException("A questão deve ter exatamente 1 alternativa correta (encontradas: " + corretas + ")");
        }

        Questao questao = Questao.builder()
                .enunciado(request.enunciado())
                .tipo(request.tipo())
                .dificuldade(request.dificuldade())
                .tipoUso(request.tipoUso())
                .nivelEnsino(request.nivelEnsino())
                .explicacao(request.explicacao())
                .disciplina(disciplina)
                .criadoPor(professorId)
                .ativa(true)
                .build();

        Questao salva = questaoRepository.save(questao);

        List<Alternativa> alternativas = new ArrayList<>();
        for (CriarQuestaoRequest.AlternativaRequest altReq : request.alternativas()) {
            Alternativa alt = Alternativa.builder()
                    .texto(altReq.texto())
                    .correta(altReq.correta())
                    .ordem(altReq.ordem())
                    .questao(salva)
                    .build();
            alternativas.add(alt);
        }
        salva.setAlternativas(alternativas);
        return QuestaoResponse.from(questaoRepository.save(salva), true);
    }

    @Transactional
    public QuestaoResponse atualizar(UUID id, CriarQuestaoRequest request, UUID professorId) {
        Questao questao = questaoRepository.findById(id)
                .filter(Questao::isAtiva)
                .orElseThrow(() -> new RuntimeException("Questão não encontrada: " + id));

        if (request.enunciado() != null && !request.enunciado().isBlank()) {
            questao.setEnunciado(request.enunciado());
        }
        if (request.tipo() != null && !request.tipo().isBlank()) {
            questao.setTipo(request.tipo());
        }
        if (request.dificuldade() != null && !request.dificuldade().isBlank()) {
            questao.setDificuldade(request.dificuldade());
        }
        if (request.tipoUso() != null && !request.tipoUso().isBlank()) {
            questao.setTipoUso(request.tipoUso());
        }
        if (request.nivelEnsino() != null && !request.nivelEnsino().isBlank()) {
            questao.setNivelEnsino(request.nivelEnsino());
        }
        if (request.explicacao() != null) {
            questao.setExplicacao(request.explicacao());
        }
        if (request.disciplinaId() != null) {
            Disciplina disciplina = disciplinaRepository.findById(request.disciplinaId())
                    .orElseThrow(() -> new IllegalArgumentException("Disciplina não encontrada: " + request.disciplinaId()));
            questao.setDisciplina(disciplina);
        }

        // Atualiza alternativas se fornecidas — orphanRemoval garante a remoção das antigas
        if (request.alternativas() != null && !request.alternativas().isEmpty()) {
            long corretas = request.alternativas().stream()
                    .filter(CriarQuestaoRequest.AlternativaRequest::correta).count();
            if (corretas != 1) {
                throw new IllegalArgumentException(
                        "A questão deve ter exatamente 1 alternativa correta (encontradas: " + corretas + ")");
            }
            questao.getAlternativas().clear();
            for (CriarQuestaoRequest.AlternativaRequest altReq : request.alternativas()) {
                Alternativa alt = Alternativa.builder()
                        .texto(altReq.texto())
                        .correta(altReq.correta())
                        .ordem(altReq.ordem())
                        .questao(questao)
                        .build();
                questao.getAlternativas().add(alt);
            }
        }

        return QuestaoResponse.from(questaoRepository.save(questao), true);
    }

    @Transactional
    public void inativar(UUID id, UUID professorId) {
        Questao questao = questaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Questão não encontrada: " + id));
        questao.setAtiva(false);
        questaoRepository.save(questao);
    }

    public Page<QuestaoResponse> minhas(UUID professorId, Pageable pageable, boolean incluirGabarito) {
        return questaoRepository.findByCriadoPorAndAtivaTrue(professorId, pageable)
                .map(q -> QuestaoResponse.from(q, incluirGabarito));
    }

    public List<AssuntoDto> listarAssuntos(UUID disciplinaId) {
        if (disciplinaId != null) {
            return assuntoRepository.findByDisciplinaIdOrderByNomeAsc(disciplinaId).stream()
                    .map(AssuntoDto::from)
                    .collect(Collectors.toList());
        }
        return assuntoRepository.findAllByOrderByNomeAsc().stream()
                .map(AssuntoDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssuntoDto criarAssunto(String nome, UUID disciplinaId) {
        Disciplina disciplina = disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina não encontrada: " + disciplinaId));
        if (assuntoRepository.existsByNomeAndDisciplinaId(nome, disciplinaId)) {
            throw new IllegalArgumentException("Assunto já existe nesta disciplina: " + nome);
        }
        Assunto assunto = Assunto.builder()
                .nome(nome)
                .disciplina(disciplina)
                .build();
        return AssuntoDto.from(assuntoRepository.save(assunto));
    }

    // ── Favoritas / marcadas para revisão ─────────────────────────────────────

    @Transactional
    public void favoritar(UUID alunoId, UUID questaoId) {
        if (!questaoRepository.existsById(questaoId)) {
            throw new RuntimeException("Questão não encontrada: " + questaoId);
        }
        if (!favoritaRepository.existsByAlunoIdAndQuestaoId(alunoId, questaoId)) {
            favoritaRepository.save(QuestaoFavorita.builder()
                    .alunoId(alunoId)
                    .questaoId(questaoId)
                    .build());
        }
    }

    @Transactional
    public void desfavoritar(UUID alunoId, UUID questaoId) {
        favoritaRepository.deleteByAlunoIdAndQuestaoId(alunoId, questaoId);
    }

    /** Lista as questões marcadas para revisão pelo aluno (com gabarito, pois é estudo dirigido). */
    public List<QuestaoResponse> listarFavoritas(UUID alunoId) {
        List<UUID> ids = favoritaRepository.findByAlunoIdOrderByCriadoEmDesc(alunoId).stream()
                .map(QuestaoFavorita::getQuestaoId)
                .toList();
        if (ids.isEmpty()) return List.of();
        return questaoRepository.findAllById(ids).stream()
                .filter(Questao::isAtiva)
                .map(q -> QuestaoResponse.from(q, true))
                .collect(Collectors.toList());
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
