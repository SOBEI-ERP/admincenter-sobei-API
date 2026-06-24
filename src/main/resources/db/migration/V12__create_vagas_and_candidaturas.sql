-- Enum para status da vaga
CREATE TYPE status_vaga AS ENUM ('aberta', 'pausada', 'fechada');

-- Enum para modalidade
CREATE TYPE modalidade_vaga AS ENUM ('presencial', 'hibrido', 'remoto');

-- Enum para tipo de contrato
CREATE TYPE tipo_contrato AS ENUM ('clt', 'estagio', 'pj', 'temporario');

-- Tabela de vagas
CREATE TABLE vagas (
    id                SERIAL PRIMARY KEY,
    titulo            VARCHAR(255) NOT NULL,
    departamento      VARCHAR(100) NOT NULL,
    unidade           VARCHAR(100) NOT NULL,
    descricao         TEXT NOT NULL,
    requisitos        TEXT NOT NULL,
    beneficios        TEXT,
    modalidade        modalidade_vaga NOT NULL DEFAULT 'presencial',
    tipo_contrato     tipo_contrato NOT NULL DEFAULT 'clt',
    status            status_vaga NOT NULL DEFAULT 'aberta',
    admin_id          INT REFERENCES administradores(id) ON DELETE SET NULL,
    data_criacao      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vagas_unidade ON vagas(unidade);
CREATE INDEX idx_vagas_status ON vagas(status);

-- Trigger de atualização automática de data_atualizacao
CREATE OR REPLACE FUNCTION update_vagas_data_atualizacao()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_vagas_data_atualizacao
    BEFORE UPDATE ON vagas
    FOR EACH ROW
    EXECUTE FUNCTION update_vagas_data_atualizacao();

-- Tabela de candidaturas
CREATE TABLE candidaturas (
    id                 SERIAL PRIMARY KEY,
    vaga_id            INT NOT NULL REFERENCES vagas(id) ON DELETE CASCADE,
    nome_completo      VARCHAR(255) NOT NULL,
    email              VARCHAR(150) NOT NULL,
    telefone           VARCHAR(20) NOT NULL,
    carta_apresentacao TEXT,
    curriculo_path     VARCHAR(500) NOT NULL,
    curriculo_nome     VARCHAR(255) NOT NULL,
    data_envio         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_candidaturas_vaga_id ON candidaturas(vaga_id);
