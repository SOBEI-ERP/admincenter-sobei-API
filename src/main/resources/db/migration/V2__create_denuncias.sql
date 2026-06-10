-- CRIAÇÃO DOS ENUMS
CREATE TYPE tipo_denuncia AS ENUM ('anonima', 'identificada');
CREATE TYPE estado_denuncia AS ENUM ('aberta', 'em_andamento', 'fechada', 'arquivada', 'reaberta');
CREATE TYPE tipo_conclusao AS ENUM ('final', 'arquivamento');

-- Tabela de Administradores
CREATE TABLE administradores (
    id SERIAL PRIMARY KEY,
    usuario VARCHAR(100) UNIQUE NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabela Principal de Denúncias
CREATE TABLE denuncias (
    id SERIAL PRIMARY KEY,
    protocolo VARCHAR(20) UNIQUE NOT NULL,
    tipo tipo_denuncia NOT NULL,
    unidade VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    envolvidos TEXT,
    testemunhas TEXT,
    estado estado_denuncia NOT NULL DEFAULT 'aberta',
    data_abertura TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ultima_alteracao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de Dados dos Denunciantes
CREATE TABLE denunciantes_identificados (
    denuncia_id INT PRIMARY KEY REFERENCES denuncias(id) ON DELETE CASCADE,
    nome_completo VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL,
    telefone VARCHAR(20) NOT NULL
);

-- Tabela de Medidas Adotadas
CREATE TABLE medidas_adotadas (
    id SERIAL PRIMARY KEY,
    denuncia_id INT NOT NULL REFERENCES denuncias(id) ON DELETE CASCADE,
    admin_id INT REFERENCES administradores(id) ON DELETE SET NULL,
    descricao TEXT NOT NULL,
    data_registro TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de Conclusões e Arquivamentos
CREATE TABLE conclusoes_denuncia (
    denuncia_id INT PRIMARY KEY REFERENCES denuncias(id) ON DELETE CASCADE,
    admin_id INT REFERENCES administradores(id) ON DELETE SET NULL,
    tipo_conclusao tipo_conclusao NOT NULL,
    relatorio TEXT NOT NULL,
    data_conclusao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de Histórico de Estados (Auditoria)
CREATE TABLE historico_estados (
    id SERIAL PRIMARY KEY,
    denuncia_id INT NOT NULL REFERENCES denuncias(id) ON DELETE CASCADE,
    estado_anterior estado_denuncia,
    estado_novo estado_denuncia NOT NULL,
    admin_id INT REFERENCES administradores(id) ON DELETE SET NULL,
    data_alteracao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Índices para performance
CREATE INDEX idx_denuncias_protocolo ON denuncias(protocolo);
CREATE INDEX idx_denuncias_estado ON denuncias(estado);

-- Trigger para atualizar ultima_alteracao
CREATE OR REPLACE FUNCTION update_ultima_alteracao_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.ultima_alteracao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_denuncias_ultima_alteracao
    BEFORE UPDATE ON denuncias
    FOR EACH ROW
    EXECUTE PROCEDURE update_ultima_alteracao_column();
