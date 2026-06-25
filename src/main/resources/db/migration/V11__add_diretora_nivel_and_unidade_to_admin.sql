-- Adiciona 'diretora' ao enum nivel_admin
ALTER TYPE nivel_admin ADD VALUE 'diretora';

-- Adiciona coluna unidade ao administradores (nullable para admin/suporte)
ALTER TABLE administradores ADD COLUMN unidade VARCHAR(100);
