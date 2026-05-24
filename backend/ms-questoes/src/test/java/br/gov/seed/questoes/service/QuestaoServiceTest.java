package br.gov.seed.questoes.service;

import br.gov.seed.questoes.dto.DisciplinaDto;
import br.gov.seed.questoes.dto.QuestaoResponse;
import br.gov.seed.questoes.entity.Alternativa;
import br.gov.seed.questoes.entity.Disciplina;
import br.gov.seed.questoes.entity.Questao;
import br.gov.seed.questoes.repository.DisciplinaRepository;
import br.gov.seed.questoes.repository.QuestaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestaoService — testes unitários")
class QuestaoServiceTest {

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @InjectMocks
    private QuestaoService questaoService;

    private Disciplina disciplina;
    private Questao questao;

    @BeforeEach
    void setUp() {
        disciplina = Disciplina.builder()
                .id(UUID.randomUUID())
                .nome("Matemática")
                .build();

        Alternativa alt = Alternativa.builder()
                .id(UUID.randomUUID())
                .texto("Alternativa A")
                .correta(true)
                .ordem(1)
                .build();

        questao = Questao.builder()
                .id(UUID.randomUUID())
                .enunciado("Quanto é 2 + 2?")
                .tipo("MULTIPLA_ESCOLHA")
                .dificuldade("FACIL")
                .tipoUso("AMBOS")
                .disciplina(disciplina)
                .ativa(true)
                .criadoPor(UUID.randomUUID())
                .alternativas(List.of(alt))
                .build();

        alt.setQuestao(questao);
    }

    // ── listar ────────────────────────────────────────────────

    @Test
    @DisplayName("listar retorna página de questões mapeadas para DTO")
    void listar_retornaPageDTO() {
        Page<Questao> page = new PageImpl<>(List.of(questao));
        when(questaoRepository.filtrar(any(), any(), any(), any())).thenReturn(page);

        Page<QuestaoResponse> resultado = questaoService.listar(null, null, null, PageRequest.of(0, 20));

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).enunciado()).isEqualTo("Quanto é 2 + 2?");
        assertThat(resultado.getContent().get(0).disciplina()).isEqualTo("Matemática");
    }

    @Test
    @DisplayName("listar com filtros passa parâmetros corretos ao repositório")
    void listar_comFiltros_passaParametros() {
        UUID disciplinaId = UUID.randomUUID();
        Page<Questao> page = new PageImpl<>(List.of());
        when(questaoRepository.filtrar(eq(disciplinaId), eq("FACIL"), eq("MULTIPLA_ESCOLHA"), any()))
                .thenReturn(page);

        Page<QuestaoResponse> resultado = questaoService.listar(
                disciplinaId, "FACIL", "MULTIPLA_ESCOLHA", PageRequest.of(0, 20));

        assertThat(resultado.getContent()).isEmpty();
    }

    // ── buscarPorId ───────────────────────────────────────────

    @Test
    @DisplayName("buscarPorId retorna DTO quando questão existe e está ativa")
    void buscarPorId_questaoAtiva_retornaDTO() {
        when(questaoRepository.findById(questao.getId())).thenReturn(Optional.of(questao));

        QuestaoResponse resp = questaoService.buscarPorId(questao.getId());

        assertThat(resp.id()).isEqualTo(questao.getId());
        assertThat(resp.alternativas()).hasSize(1);
    }

    @Test
    @DisplayName("buscarPorId lança exceção quando questão não existe")
    void buscarPorId_naoExiste_lancaExcecao() {
        UUID id = UUID.randomUUID();
        when(questaoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questaoService.buscarPorId(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("buscarPorId lança exceção quando questão está inativa")
    void buscarPorId_inativa_lancaExcecao() {
        questao.setAtiva(false);
        when(questaoRepository.findById(questao.getId())).thenReturn(Optional.of(questao));

        assertThatThrownBy(() -> questaoService.buscarPorId(questao.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    // ── listarDisciplinas ─────────────────────────────────────

    @Test
    @DisplayName("listarDisciplinas retorna todas as disciplinas como DTO")
    void listarDisciplinas_retornaLista() {
        when(disciplinaRepository.findAll()).thenReturn(List.of(disciplina));

        List<DisciplinaDto> resultado = questaoService.listarDisciplinas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nome()).isEqualTo("Matemática");
    }

    @Test
    @DisplayName("listarDisciplinas retorna lista vazia quando não há disciplinas")
    void listarDisciplinas_vazio_retornaListaVazia() {
        when(disciplinaRepository.findAll()).thenReturn(List.of());

        List<DisciplinaDto> resultado = questaoService.listarDisciplinas();

        assertThat(resultado).isEmpty();
    }

    // ── total ─────────────────────────────────────────────────

    @Test
    @DisplayName("total retorna contagem do repositório")
    void total_retornaContagem() {
        when(questaoRepository.countByAtivaTrue()).thenReturn(2740L);

        long total = questaoService.total();

        assertThat(total).isEqualTo(2740L);
    }
}
