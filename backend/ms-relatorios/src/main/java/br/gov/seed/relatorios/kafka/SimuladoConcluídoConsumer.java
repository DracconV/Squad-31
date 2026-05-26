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

            alunoService.atualizarDesempenho(
                evento.alunoId(),
                evento.turmaId(),
                evento.disciplina(),
                evento.acertos(),
                evento.total()
            );

            turmaService.recalcularDesempenhoTurma(evento.turmaId());

            ack.acknowledge();
            log.info("Evento processado com sucesso");
        } catch (Exception e) {
            log.error("Erro ao processar evento do Kafka", e);
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

