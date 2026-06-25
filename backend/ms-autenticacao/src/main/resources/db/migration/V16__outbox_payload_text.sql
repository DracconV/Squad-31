-- O payload do outbox_event é sempre uma string JSON: o ms-simulados grava via JPA
-- (entidade String) e o relay (OutboxPublisher) lê como String e publica no Kafka.
-- Nenhum serviço usa operadores jsonb sobre a coluna. O tipo jsonb forçava cast no
-- INSERT do Hibernate (character varying -> jsonb) e quebrava a finalização do simulado.
-- text é suficiente e compatível com leitura/escrita como string.
ALTER TABLE outbox_event ALTER COLUMN payload TYPE text;
