-- =============================================
-- V23: Unificar coluna oficina em inscricoes_congresso
-- =============================================

ALTER TABLE inscricoes_congresso
    ADD COLUMN IF NOT EXISTS oficina VARCHAR(255);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'inscricoes_congresso' 
          AND column_name = 'oficina_manha'
    ) THEN
        UPDATE inscricoes_congresso
        SET oficina = COALESCE(oficina_manha, oficina_tarde)
        WHERE oficina IS NULL AND (oficina_manha IS NOT NULL OR oficina_tarde IS NOT NULL);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_inscricoes_congresso_oficina ON inscricoes_congresso(oficina);
