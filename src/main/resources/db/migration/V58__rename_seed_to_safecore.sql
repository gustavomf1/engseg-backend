-- Atualiza empresa e usuário admin seed (gravados por V1__init.sql) de EngSeg para SafeCore
UPDATE empresa
SET razao_social = 'SafeCore Administração',
    nome_fantasia = 'SafeCore',
    email = 'contato@safecoreteste.online'
WHERE id = '00000000-0000-0000-0000-000000000001';

UPDATE usuario
SET email = 'admin@safecoreteste.online'
WHERE id = '00000000-0000-0000-0000-000000000001'
  AND email = 'admin@engseg.com';
