-- =============================================
-- V22: Adicionar oficinas do congresso (Manhã e Tarde)
-- =============================================

ALTER TABLE inscricoes_congresso
    ADD COLUMN oficina_manha VARCHAR(255),
    ADD COLUMN oficina_tarde VARCHAR(255);

CREATE INDEX idx_inscricoes_congresso_oficina_manha ON inscricoes_congresso(oficina_manha);
CREATE INDEX idx_inscricoes_congresso_oficina_tarde ON inscricoes_congresso(oficina_tarde);
