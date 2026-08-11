-- Migration V17: Criação da estrutura de Chamados de Suporte

CREATE TYPE status_chamado AS ENUM ('aberto', 'em_andamento', 'aguardando_informacao', 'concluido', 'cancelado');
CREATE TYPE prioridade_chamado AS ENUM ('baixa', 'media', 'alta', 'urgente');

CREATE TABLE chamados (
    id                SERIAL PRIMARY KEY,
    titulo            VARCHAR(255) NOT NULL,
    descricao         TEXT NOT NULL,
    solicitante       VARCHAR(255) NOT NULL,
    prioridade        prioridade_chamado NOT NULL DEFAULT 'media',
    status            status_chamado NOT NULL DEFAULT 'aberto',
    prazo_conclusao   DATE,
    plano_acao        TEXT,
    resolucao         TEXT,
    data_encerramento TIMESTAMP WITH TIME ZONE,
    usuario_id        INT REFERENCES administradores(id) ON DELETE SET NULL,
    data_criacao      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ultima_alteracao  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chamados_status ON chamados(status);
CREATE INDEX idx_chamados_prioridade ON chamados(prioridade);
CREATE INDEX idx_chamados_solicitante ON chamados(solicitante);

CREATE TRIGGER update_chamados_ultima_alteracao
    BEFORE UPDATE ON chamados
    FOR EACH ROW
    EXECUTE PROCEDURE update_ultima_alteracao_column();
