-- =============================================================================
-- V25: Otimizar índices funcionais e compostos para consultas de participantes
--      do Congresso SOBEI (ordenamento alfabético, busca por CPF/e-mail/nome,
--      filtros por unidade/OSC, cálculo de cotas de oficinas e check-in duplo)
-- =============================================================================

-- 1. Índice de ordenação alfabética direta (elimina full sort em ORDER BY nome_completo ASC)
CREATE INDEX IF NOT EXISTS idx_inscricoes_congresso_nome_completo
    ON inscricoes_congresso (nome_completo ASC);

-- 2. Índices funcionais para buscas case-insensitive e CPF sem máscara
CREATE INDEX IF NOT EXISTS idx_inscricoes_congresso_lower_nome
    ON inscricoes_congresso (LOWER(nome_completo));

CREATE INDEX IF NOT EXISTS idx_inscricoes_congresso_lower_email
    ON inscricoes_congresso (LOWER(TRIM(email)));

CREATE INDEX IF NOT EXISTS idx_inscricoes_congresso_cpf_limpo
    ON inscricoes_congresso (REPLACE(REPLACE(cpf, '.', ''), '-', ''));

-- 3. Índice composto para filtragem de Coordenadoras/Diretoras e filtros por OSC + Unidade já ordenado por nome
CREATE INDEX IF NOT EXISTS idx_inscricoes_congresso_osc_unidade_nome
    ON inscricoes_congresso (LOWER(tipo_osc), LOWER(TRIM(unidade)), nome_completo ASC);

-- 4. Índice parcial para participantes elegíveis a certificado em lote (check-in em ambos os dias)
CREATE INDEX IF NOT EXISTS idx_inscricoes_congresso_checkin_ambos
    ON inscricoes_congresso (nome_completo ASC)
    WHERE presente_dia11 = TRUE AND presente_dia12 = TRUE;

-- 5. Índice parcial para validação rápida de cotas de oficinas por tipo de OSC e unidade
CREATE INDEX IF NOT EXISTS idx_inscricoes_congresso_com_oficina
    ON inscricoes_congresso (LOWER(tipo_osc), LOWER(TRIM(unidade)))
    WHERE oficina IS NOT NULL OR oficina_manha IS NOT NULL OR oficina_tarde IS NOT NULL;
