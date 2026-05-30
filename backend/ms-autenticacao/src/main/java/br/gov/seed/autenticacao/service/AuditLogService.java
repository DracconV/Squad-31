package br.gov.seed.autenticacao.service;

import br.gov.seed.autenticacao.entity.AuditLog;
import br.gov.seed.autenticacao.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Grava uma entrada no audit_log de forma assíncrona
     * para não impactar a latência do endpoint.
     */
    @Async
    public void registrar(UUID usuarioId,
                          String acao,
                          String entidade,
                          UUID entidadeId,
                          Map<String, Object> detalhes) {
        try {
            AuditLog log = AuditLog.builder()
                    .usuarioId(usuarioId)
                    .acao(acao)
                    .entidade(entidade)
                    .entidadeId(entidadeId)
                    .detalhes(detalhes)
                    .timestamp(LocalDateTime.now())
                    .build();
            auditLogRepository.save(log);
        } catch (Exception e) {
            // Audit nunca pode derrubar o fluxo principal
            log.warn("Falha ao gravar audit_log: {}", e.getMessage());
        }
    }
}
