ALTER TABLE nao_conformidade ADD COLUMN IF NOT EXISTS titulo VARCHAR(255);
ALTER TABLE nao_conformidade ADD COLUMN IF NOT EXISTS localizacao VARCHAR(255);
ALTER TABLE desvio ADD COLUMN IF NOT EXISTS titulo VARCHAR(255);
ALTER TABLE desvio ADD COLUMN IF NOT EXISTS localizacao VARCHAR(255);

-- Set default values for existing rows
UPDATE nao_conformidade SET titulo = 'Sem título' WHERE titulo IS NULL;
UPDATE nao_conformidade SET localizacao = 'Não informado' WHERE localizacao IS NULL;
UPDATE desvio SET titulo = 'Sem título' WHERE titulo IS NULL;
UPDATE desvio SET localizacao = 'Não informado' WHERE localizacao IS NULL;

-- Now make them NOT NULL
ALTER TABLE nao_conformidade ALTER COLUMN titulo SET NOT NULL;
ALTER TABLE nao_conformidade ALTER COLUMN localizacao SET NOT NULL;
ALTER TABLE desvio ALTER COLUMN titulo SET NOT NULL;
ALTER TABLE desvio ALTER COLUMN localizacao SET NOT NULL;
