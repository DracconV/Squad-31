package br.gov.seed.relatorios.kafka;

import br.gov.seed.relatorios.service.DesempenhoAlunoService;
import br.gov.seed.relatorios.service.DesempenhoTurmaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimuladoConcluídoConsumer {

    private final DesempenhoAlunoService alunoService;
    private final DesempenhoTurmaService turmaService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "simulado-eventos",
        groupId = "ms-relatorios",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void processarSimuladoConcluido(@Payload String mensagem, Acknowledgment ack) {
        try {
            log.info("Recebido evento de conclusão: {}", mensagem);

            var evento = objectMapper.readValue(mensagem, SimuladoConcluidoEvento.class);

            // O tópico também carrega eventos TEMPO_ESGOTADO (sem aluno/disciplina) —
            // ignora qualquer mensagem que não seja uma conclusão completa.
            if (evento.alunoId() == null || evento.disciplina() == null
                    || evento.acertos() == null || evento.total() == null) {
                log.info("Evento ignorado (não é conclusão de simulado): {}", mensagem);
                return;
            }

            alunoService.atualizarDesempenho(
                evento.alunoId(),
                evento.turmaId(),
                evento.disciplina(),
                evento.acertos(),
                evento.total()
            );

            // Simulado avulso (sem turma) não tem agregado de turma para recalcular
            if (evento.turmaId() != null) {
                turmaService.recalcularDesempenhoTurma(evento.turmaId());
            }

            log.info("Evento processado com sucesso");
        } catch (Exception e) {
            // Loga e segue — sem ack a mensagem-veneno seria reprocessada para sempre
            log.error("Erro ao processar evento do Kafka — mensagem descartada: {}", mensagem, e);
        } finally {
            ack.acknowledge();
        }
    }

    public record SimuladoConcluidoEvento(
        UUID alunoId,
        UUID turmaId,
        String disciplina,
        Integer acertos,
        Integer total
    ) {}
}

