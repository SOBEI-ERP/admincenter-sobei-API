-- =============================================
-- V21: Adicionar confirmação de presença separada para Dia 11 e Dia 12 de Setembro
-- =============================================

ALTER TABLE inscricoes_congresso
    ADD COLUMN presente_dia11 BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN data_presenca_dia11 TIMESTAMP WITH TIME ZONE,
    ADD COLUMN presente_dia12 BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN data_presenca_dia12 TIMESTAMP WITH TIME ZONE;

-- Migrar registros antigos se presente estava marcado como true
UPDATE inscricoes_congresso 
SET presente_dia11 = presente, data_presenca_dia11 = data_presenca
WHERE presente = TRUE;

CREATE INDEX idx_inscricoes_congresso_presenca_dia11 ON inscricoes_congresso(presente_dia11);
CREATE INDEX idx_inscricoes_congresso_presenca_dia12 ON inscricoes_congresso(presente_dia12);
