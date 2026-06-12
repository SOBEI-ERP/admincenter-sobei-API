CREATE TYPE nivel_admin AS ENUM ('admin', 'suporte');

ALTER TABLE administradores ADD COLUMN nivel nivel_admin NOT NULL DEFAULT 'admin';

UPDATE administradores SET nivel = 'suporte' WHERE usuario = 'joserodrigues';
