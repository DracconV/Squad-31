package br.com.seed.notificacoes.kafka;

import br.com.seed.notificacoes.domain.enums.NotificationChannel;
import br.com.seed.notificacoes.domain.enums.NotificationType;
import br.com.seed.notificacoes.dto.CreateNotificationRequest;
import br.com.seed.notificacoes.service.NotificationService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Quando um simulado é liberado para uma turma (evento publicado pelo ms-simulados),
 * notifica automaticamente cada aluno da turma. O fan-out lê a tabela aluno_turma do
 * banco compartilhado. O tópico simulado-eventos também carrega eventos de conclusão
 * (sem o campo "tipo"), que são ignorados aqui.
 */
@Component
public class SimuladoLiberadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(SimuladoLiberadoConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    public SimuladoLiberadoConsumer(NotificationService notificationService,
                                    ObjectMapper objectMapper,
                                    JdbcTemplate jdbcTemplate) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @KafkaListener(topics = "simulado-eventos", groupId = "ms-notificacoes-simulados")
    public void onSimuladoEvento(String mensagem) {
        try {
            EventoSimulado evento = objectMapper.readValue(mensagem, EventoSimulado.class);

            // Só reage a liberação de simulado; eventos de conclusão não têm "tipo".
            if (!"SIMULADO_LIBERADO".equals(evento.tipo())
                    || evento.turmaId() == null || evento.turmaId().isBlank()) {
                return;
            }

            UUID turmaId = UUID.fromString(evento.turmaId());
            // UUID nativo no bind evita erro de operador uuid = text no PostgreSQL.
            List<UUID> alunos = jdbcTemplate.query(
                    "SELECT aluno_id FROM aluno_turma WHERE turma_id = ?",
                    (rs, i) -> UUID.fromString(rs.getString(1)),
                    turmaId);

            if (alunos.isEmpty()) {
                log.info("Simulado liberado para turma {} sem alunos vinculados — nada a notificar", turmaId);
                return;
            }

            String titulo = evento.titulo() != null && !evento.titulo().isBlank()
                    ? evento.titulo() : "um novo simulado";
            UUID simuladoId = evento.simuladoId() != null && !evento.simuladoId().isBlank()
                    ? UUID.fromString(evento.simuladoId()) : null;

            for (UUID alunoId : alunos) {
                notificationService.create(new CreateNotificationRequest(
                        alunoId,
                        "Novo simulado disponível",
                        "O simulado \"" + titulo + "\" foi liberado para a sua turma.",
                        NotificationType.SIMULADO_LIBERADO,
                        NotificationChannel.IN_APP,
                        simuladoId,
                        "SIMULADO"
                ));
            }
            log.info("Simulado {} liberado: {} aluno(s) da turma {} notificados",
                    simuladoId, alunos.size(), turmaId);
        } catch (Exception e) {
            // Loga e segue — não trava o consumer com mensagem inválida
            log.error("Erro ao processar evento simulado-eventos: {}", mensagem, e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EventoSimulado(String tipo, String simuladoId, String turmaId, String titulo) {}
}
