-- =============================================
-- V20: Migrar usuários 'admin' para 'dp' e resetar usuário joseantonio
-- =============================================

-- 1. Migrar administradores com nível 'admin' para 'dp' (agora que o enum 'dp' foi commitado na V18)
UPDATE administradores SET nivel = 'dp' WHERE nivel = 'admin';

-- 2. Remove qualquer registro anterior com este e-mail ou nome de usuário
DELETE FROM administradores WHERE email = 'joseantonio@sobei.org.br' OR usuario = 'joserodrigues';

-- 3. Insere novamente o usuário com nível SUPORTE e novo hash BCrypt
INSERT INTO administradores (usuario, email, senha_hash, nivel)
VALUES (
    'joserodrigues',
    'joseantonio@sobei.org.br',
    '$2a$10$ZUaNsEILa5r5Si0cbpn9SuvqBbNXJS3uLzyNgUQFk6d4kcBQVefsa',
    'suporte'
);
