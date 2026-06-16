-- Flyway não pode executar ADD VALUE em transação junto com uso do novo valor.
-- Esta migration roda fora de transação (non-transactional).
-- @UNDO: Não é possível remover valores de enum no PostgreSQL.

ALTER TYPE nivel_prioridade ADD VALUE IF NOT EXISTS 'neutra';
