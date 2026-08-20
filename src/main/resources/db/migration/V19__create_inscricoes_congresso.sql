-- =============================================
-- V19: Criar tabela de inscrições do congresso
-- =============================================

CREATE TABLE inscricoes_congresso (
    id              SERIAL PRIMARY KEY,
    nome_completo   VARCHAR(255) NOT NULL,
    cpf             VARCHAR(14) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    tipo_osc        VARCHAR(20) NOT NULL, -- 'SOBEI' ou 'OUTRA'
    unidade         VARCHAR(100),        -- Obrigatório se tipo_osc = 'SOBEI'
    outra_osc       VARCHAR(255),        -- Obrigatório se tipo_osc = 'OUTRA'
    presente        BOOLEAN NOT NULL DEFAULT FALSE,
    data_inscricao  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    data_presenca   TIMESTAMP WITH TIME ZONE
);

-- Índices para buscas e filtros rápidos
CREATE INDEX idx_inscricoes_congresso_cpf ON inscricoes_congresso(cpf);
CREATE INDEX idx_inscricoes_congresso_email ON inscricoes_congresso(email);
CREATE INDEX idx_inscricoes_congresso_unidade ON inscricoes_congresso(unidade);
CREATE INDEX idx_inscricoes_congresso_tipo_osc ON inscricoes_congresso(tipo_osc);
CREATE INDEX idx_inscricoes_congresso_presente ON inscricoes_congresso(presente);
