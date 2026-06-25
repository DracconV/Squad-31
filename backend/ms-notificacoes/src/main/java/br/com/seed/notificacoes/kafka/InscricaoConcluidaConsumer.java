package br.com.seed.notificacoes.kafka;

import br.com.seed.notificacoes.domain.enums.NotificationChannel;
import br.com.seed.notificacoes.domain.enums.NotificationType;
import br.com.seed.notificacoes.dto.CreateNotificationRequest;
import br.com.seed.notificacoes.service.NotificationService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Gera notificação automática quando um aluno conclui um curso.
 * Consome o mesmo tópico do ms-certificados (group-id próprio = ambos recebem).
 */
@Component
public class InscricaoConcluidaConsumer {

    private static final Logger log = LoggerFactory.getLogger(InscricaoConcluidaConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public InscricaoConcluidaConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "inscricao-concluida", groupId = "ms-notificacoes")
    public void onInscricaoConcluida(String mensagem) {
        try {
            EventoInscricao evento = objectMapper.readValue(mensagem, EventoInscricao.class);
            if (evento.alunoId() == null || evento.alunoId().isBlank()) {
                log.warn("Evento inscricao-concluida sem alunoId, ignorado: {}", mensagem);
                return;
            }

            String curso = evento.nomeCurso() != null ? evento.nomeCurso() : "um curso";
            notificationService.create(new CreateNotificationRequest(
                    UUID.fromString(evento.alunoId()),
                    "Certificado disponível",
                    "Parabéns! Você concluiu " + curso + ". Seu certificado já está disponível.",
                    NotificationType.RESULTADO_DISPONIVEL,
                    NotificationChannel.IN_APP,
                    evento.cursoId() != null && !evento.cursoId().isBlank() ? UUID.fromString(evento.cursoId()) : null,
                    "CURSO"
            ));
            log.info("Notificação de conclusão criada para aluno={} curso={}", evento.alunoId(), curso);
        } catch (Exception e) {
            // Loga e segue — não trava o consumer com mensagem inválida
            log.error("Erro ao processar evento inscricao-concluida: {}", mensagem, e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EventoInscricao(String alunoId, String cursoId, String nomeAluno, String nomeCurso) {}
}
