-- Tabela para armazenar candidaturas movidas ao fechar uma vaga (Banco de Talentos)
CREATE TABLE banco_talentos (
    id                  SERIAL PRIMARY KEY,
    vaga_id             INT NOT NULL REFERENCES vagas(id) ON DELETE CASCADE,
    nome_completo       VARCHAR(255) NOT NULL,
    email               VARCHAR(150) NOT NULL,
    telefone            VARCHAR(20) NOT NULL,
    carta_apresentacao  TEXT,
    curriculo_path      VARCHAR(500) NOT NULL,
    curriculo_nome      VARCHAR(255) NOT NULL,
    data_envio_original TIMESTAMP WITH TIME ZONE NOT NULL,
    data_movimentacao   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_banco_talentos_vaga_id ON banco_talentos(vaga_id);
