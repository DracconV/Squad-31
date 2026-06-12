package br.gov.seed.questoes.service;

import br.gov.seed.questoes.entity.Alternativa;
import br.gov.seed.questoes.entity.Disciplina;
import br.gov.seed.questoes.entity.Questao;
import br.gov.seed.questoes.repository.DisciplinaRepository;
import br.gov.seed.questoes.repository.QuestaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnemImporterService — testes unitários de salvarQuestao")
class EnemImporterServiceTest {

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private EnemImporterService service;

    private EnemImporterService.EnemQuestion questaoCompleta;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseUrl", "https://api.enem.dev/v1");
        ReflectionTestUtils.setField(service, "autoImport", false);

        EnemImporterService.EnemAlternative altA = new EnemImporterService.EnemAlternative();
        altA.setLetter("A");
        altA.setText("Alternativa A");
        altA.setIsCorrect(true);

        EnemImporterService.EnemAlternative altB = new EnemImporterService.EnemAlternative();
        altB.setLetter("B");
        altB.setText("Alternativa B");
        altB.setIsCorrect(false);

        questaoCompleta = new EnemImporterService.EnemQuestion();
        questaoCompleta.setTitle("Qual é a capital do Brasil?");
        questaoCompleta.setDiscipline("ciencias-humanas");
        questaoCompleta.setYear(2023);
        questaoCompleta.setCorrectAlternative("A");
        questaoCompleta.setAlternatives(List.of(altA, altB));
    }

    // ── salvarQuestao ─────────────────────────────────────────

    @Test
    @DisplayName("salvarQuestao persiste questão com disciplina e alternativas corretas")
    void salvarQuestao_sucesso() {
        Disciplina disciplina = Disciplina.builder()
                .id(UUID.randomUUID())
                .nome("Ciências Humanas")
                .build();
        Questao questaoSalva = Questao.builder().id(UUID.randomUUID()).build();

        when(disciplinaRepository.findByNome("Ciências Humanas")).thenReturn(Optional.of(disciplina));
        when(questaoRepository.save(any(Questao.class))).thenReturn(questaoSalva);

        service.salvarQuestao(questaoCompleta, UUID.randomUUID());

        ArgumentCaptor<Questao> captor = ArgumentCaptor.forClass(Questao.class);
        verify(questaoRepository).save(captor.capture());

        Questao salva = captor.getValue();
        assertThat(salva.getEnunciado()).contains("Qual é a capital do Brasil?");
        assertThat(salva.getTipo()).isEqualTo("MULTIPLA_ESCOLHA");
        assertThat(salva.getDificuldade()).isEqualTo("MEDIO");
        assertThat(salva.getTipoUso()).isEqualTo("AMBOS");
        assertThat(salva.isAtiva()).isTrue();
        assertThat(salva.getDisciplina().getNome()).isEqualTo("Ciências Humanas");
        assertThat(salva.getAlternativas()).hasSize(2);

        long corretas = salva.getAlternativas().stream()
                .filter(Alternativa::isCorreta).count();
        assertThat(corretas).isEqualTo(1);
    }

    @Test
    @DisplayName("salvarQuestao cria disciplina nova quando não existe no banco")
    void salvarQuestao_criaDiscipina() {
        Disciplina novaDisciplina = Disciplina.builder()
                .id(UUID.randomUUID())
                .nome("Matemática")
                .build();

        when(disciplinaRepository.findByNome("Matemática")).thenReturn(Optional.empty());
        when(disciplinaRepository.save(any(Disciplina.class))).thenReturn(novaDisciplina);
        when(questaoRepository.save(any(Questao.class))).thenReturn(Questao.builder().id(UUID.randomUUID()).build());

        questaoCompleta.setDiscipline("matematica");
        service.salvarQuestao(questaoCompleta, UUID.randomUUID());

        verify(disciplinaRepository).save(any(Disciplina.class));
        verify(questaoRepository).save(any(Questao.class));
    }

    @Test
    @DisplayName("salvarQuestao ignora questões sem alternativas")
    void salvarQuestao_semAlternativas_ignora() {
        questaoCompleta.setAlternatives(List.of());

        service.salvarQuestao(questaoCompleta, UUID.randomUUID());

        verify(questaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("salvarQuestao ignora questões com enunciado vazio")
    void salvarQuestao_enunciadoVazio_ignora() {
        questaoCompleta.setTitle(null);
        questaoCompleta.setContext(null);
        questaoCompleta.setAlternativesIntroduction(null);

        service.salvarQuestao(questaoCompleta, UUID.randomUUID());

        verify(questaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("salvarQuestao usa disciplina 'Geral' para discipline desconhecida")
    void salvarQuestao_disciplinaDesconhecida_usaGeral() {
        Disciplina geral = Disciplina.builder().id(UUID.randomUUID()).nome("Geral").build();
        when(disciplinaRepository.findByNome("Geral")).thenReturn(Optional.of(geral));
        when(questaoRepository.save(any())).thenReturn(Questao.builder().id(UUID.randomUUID()).build());

        questaoCompleta.setDiscipline("topico-desconhecido");
        service.salvarQuestao(questaoCompleta, UUID.randomUUID());

        ArgumentCaptor<Questao> captor = ArgumentCaptor.forClass(Questao.class);
        verify(questaoRepository).save(captor.capture());
        assertThat(captor.getValue().getDisciplina().getNome()).isEqualTo("Geral");
    }

    @Test
    @DisplayName("salvarQuestao inclui contexto e introdução no enunciado quando presentes")
    void salvarQuestao_montaEnunciadoCompleto() {
        when(disciplinaRepository.findByNome(anyString())).thenReturn(
                Optional.of(Disciplina.builder().id(UUID.randomUUID()).nome("Linguagens e Códigos").build()));
        when(questaoRepository.save(any())).thenReturn(Questao.builder().id(UUID.randomUUID()).build());

        questaoCompleta.setDiscipline("linguagens");
        questaoCompleta.setContext("Leia o texto a seguir:");
        questaoCompleta.setAlternativesIntroduction("Com base no texto, marque a alternativa correta:");

        service.salvarQuestao(questaoCompleta, UUID.randomUUID());

        ArgumentCaptor<Questao> captor = ArgumentCaptor.forClass(Questao.class);
        verify(questaoRepository).save(captor.capture());
        String enunciado = captor.getValue().getEnunciado();
        assertThat(enunciado).contains("Leia o texto a seguir:");
        assertThat(enunciado).contains("Com base no texto");
        assertThat(enunciado).contains("Qual é a capital do Brasil?");
    }
}
