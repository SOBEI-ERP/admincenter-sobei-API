-- 1. Adiciona a coluna email permitindo valores nulos temporariamente
ALTER TABLE administradores ADD COLUMN email VARCHAR(255);

-- 2. Popula os emails dos administradores existentes com base no nome de usuário
UPDATE administradores SET email = usuario || '@sobei.org.br';
UPDATE administradores SET email = 'joseantonio@sobei.org.br' WHERE usuario = 'joserodrigues';

-- 3. Aplica restrições NOT NULL e UNIQUE na coluna email
ALTER TABLE administradores ALTER COLUMN email SET NOT NULL;
ALTER TABLE administradores ADD CONSTRAINT unique_admin_email UNIQUE (email);
