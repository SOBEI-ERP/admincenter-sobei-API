-- Renomear valores do enum status_vaga no PostgreSQL
ALTER TYPE status_vaga RENAME VALUE 'aberta' TO 'ativo';
ALTER TYPE status_vaga RENAME VALUE 'pausada' TO 'em_selecao';
ALTER TYPE status_vaga RENAME VALUE 'fechada' TO 'fechado';

-- Atualizar o valor default na tabela de vagas
ALTER TABLE vagas ALTER COLUMN status SET DEFAULT 'ativo';
