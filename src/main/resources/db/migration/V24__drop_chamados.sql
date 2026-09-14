-- Migration V24: Remocao da estrutura de Chamados de Suporte

DROP TABLE IF EXISTS chamados CASCADE;
DROP TYPE IF EXISTS status_chamado;
DROP TYPE IF EXISTS prioridade_chamado;
