-- CRIAÇÃO DO ENUM DE PRIORIDADE
CREATE TYPE nivel_prioridade AS ENUM ('baixa', 'media', 'alta');

-- ADICIONAR COLUNA PRIORIDADE À TABELA DE DENÚNCIAS
ALTER TABLE denuncias ADD COLUMN prioridade nivel_prioridade NOT NULL DEFAULT 'baixa';
