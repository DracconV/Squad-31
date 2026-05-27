package br.gov.seed.relatorios.service;

import br.gov.seed.relatorios.entity.DesempenhoAluno;
import br.gov.seed.relatorios.repository.DesempenhoAlunoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesempenhoAlunoServiceTest {

    @Mock
    private DesempenhoAlunoRepository repository;

    @InjectMocks
    private DesempenhoAlunoService service;

    @Test
    void testAtualizarDesempenho() {
        var alunoId = UUID.randomUUID();
        var turmaId = UUID.randomUUID();
        
        var desempenho = DesempenhoAluno.builder()
            .alunoId(alunoId)
            .turmaId(turmaId)
            .disciplina("Matemática")
            .questoesAcertadas(0)
            .questoesTotal(0)
            .build();

        when(repository.findByAlunoIdAndDisciplina(alunoId, "Matemática"))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(desempenho));
        
        when(repository.save(any())).thenReturn(desempenho);

        service.atualizarDesempenho(alunoId, turmaId, "Matemática", 8, 10);

        verify(repository, times(1)).save(any(DesempenhoAluno.class));
    }

    @Test
    void testObterHistoricoAluno() {
        var alunoId = UUID.randomUUID();
        
        var desempenho1 = DesempenhoAluno.builder()
            .id(UUID.randomUUID())
            .alunoId(alunoId)
            .turmaId(UUID.randomUUID())
            .disciplina("Matemática")
            .notaMedia(BigDecimal.valueOf(7.5))
            .questoesAcertadas(8)
            .questoesTotal(10)
            .build();

        when(repository.findByAlunoId(alunoId))
            .thenReturn(java.util.List.of(desempenho1));

        var resultado = service.obterHistoricoAluno(alunoId);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(repository, times(1)).findByAlunoId(alunoId);
    }
}

