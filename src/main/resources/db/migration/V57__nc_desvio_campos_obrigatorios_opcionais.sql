ALTER TABLE nao_conformidade ALTER COLUMN descricao DROP NOT NULL;
ALTER TABLE nao_conformidade ALTER COLUMN severidade DROP NOT NULL;
ALTER TABLE nao_conformidade ALTER COLUMN probabilidade DROP NOT NULL;
ALTER TABLE nao_conformidade ALTER COLUMN nivel_risco DROP NOT NULL;
ALTER TABLE desvio ALTER COLUMN descricao DROP NOT NULL;
ALTER TABLE desvio ALTER COLUMN orientacao_realizada DROP NOT NULL;
