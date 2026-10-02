-- Tabela para armazenar mensagens/intenções de vagas enviadas pelas páginas de unidade do sobei.org.br
CREATE TABLE mensagens_unidade (
    id             SERIAL PRIMARY KEY,
    unidade        VARCHAR(100) NOT NULL,
    nome_completo  VARCHAR(255) NOT NULL,
    email          VARCHAR(150) NOT NULL,
    telefone       VARCHAR(20) NOT NULL,
    mensagem       TEXT NOT NULL,
    lida           BOOLEAN NOT NULL DEFAULT FALSE,
    data_envio     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_mensagens_unidade_unidade ON mensagens_unidade(unidade);
CREATE INDEX idx_mensagens_unidade_lida ON mensagens_unidade(lida);
