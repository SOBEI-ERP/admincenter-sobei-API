-- =============================================
-- V18: Adicionar novos níveis ao enum nivel_admin
-- =============================================

ALTER TYPE nivel_admin ADD VALUE IF NOT EXISTS 'dp';
ALTER TYPE nivel_admin ADD VALUE IF NOT EXISTS 'credenciador';
ALTER TYPE nivel_admin ADD VALUE IF NOT EXISTS 'coordenadora';
ALTER TYPE nivel_admin ADD VALUE IF NOT EXISTS 'coordenadora_evento';
