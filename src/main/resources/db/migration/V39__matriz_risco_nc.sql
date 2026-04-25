-- Adiciona novos campos como nullable para permitir o UPDATE antes de NOT NULL
ALTER TABLE nao_conformidade ADD COLUMN severidade INT;
ALTER TABLE nao_conformidade ADD COLUMN probabilidade INT;
ALTER TABLE nao_conformidade ADD COLUMN nivel_risco VARCHAR(20);

-- Converte dados existentes conforme mapeamento definido no design
UPDATE nao_conformidade SET severidade = 1, probabilidade = 1, nivel_risco = 'BAIXO'    WHERE nivel_severidade = 'BAIXO';
UPDATE nao_conformidade SET severidade = 2, probabilidade = 2, nivel_risco = 'BAIXO'    WHERE nivel_severidade = 'MEDIO';
UPDATE nao_conformidade SET severidade = 4, probabilidade = 2, nivel_risco = 'MODERADO' WHERE nivel_severidade = 'ALTO';
UPDATE nao_conformidade SET severidade = 5, probabilidade = 3, nivel_risco = 'ALTO'     WHERE nivel_severidade = 'CRITICO';

-- Aplica NOT NULL após garantir que todos os registros têm valor
ALTER TABLE nao_conformidade ALTER COLUMN severidade SET NOT NULL;
ALTER TABLE nao_conformidade ALTER COLUMN probabilidade SET NOT NULL;
ALTER TABLE nao_conformidade ALTER COLUMN nivel_risco SET NOT NULL;

-- Remove coluna antiga
ALTER TABLE nao_conformidade DROP COLUMN nivel_severidade;
