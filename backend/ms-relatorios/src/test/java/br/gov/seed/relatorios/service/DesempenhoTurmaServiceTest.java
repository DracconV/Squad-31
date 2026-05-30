package br.gov.seed.relatorios.service;

import br.gov.seed.relatorios.entity.DesempenhoAluno;
import br.gov.seed.relatorios.entity.DesempenhoTurma;
import br.gov.seed.relatorios.repository.DesempenhoAlunoRepository;
import br.gov.seed.relatorios.repository.DesempenhoTurmaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesempenhoTurmaServiceTest {

    @Mock
    private DesempenhoTurmaRepository turmaRepository;

    @Mock
    private DesempenhoAlunoRepository alunoRepository;

    @InjectMocks
    private DesempenhoTurmaService service;

    @Test
    void testRecalcularDesempenhoTurma() {
        var turmaId = UUID.randomUUID();

        var alunosDesempenho = List.of(
            DesempenhoAluno.builder()
                .alunoId(UUID.randomUUID())
                .turmaId(turmaId)
                .disciplina("Matemática")
                .notaMedia(BigDecimal.valueOf(7.5f))
                .taxaAcerto(75f)
                .build(),
            DesempenhoAluno.builder()
                .alunoId(UUID.randomUUID())
                .turmaId(turmaId)
                .disciplina("Matemática")
                .notaMedia(BigDecimal.valueOf(8.5f))
                .taxaAcerto(85f)
                .build()
        );

        var desempenhoTurma = DesempenhoTurma.builder().turmaId(turmaId).build();

        when(alunoRepository.findByTurmaId(turmaId)).thenReturn(alunosDesempenho);
        when(turmaRepository.findByTurmaId(turmaId)).thenReturn(Optional.of(desempenhoTurma));

        service.recalcularDesempenhoTurma(turmaId);

        verify(turmaRepository, times(1)).save(any(DesempenhoTurma.class));
    }
}

