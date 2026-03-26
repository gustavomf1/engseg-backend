-- Criar tabela localizacao
CREATE TABLE localizacao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    estabelecimento_id UUID NOT NULL REFERENCES estabelecimento(id),
    ativo BOOLEAN NOT NULL DEFAULT true
);

-- Adicionar coluna localizacao_id nas tabelas de ocorrencia
ALTER TABLE desvio ADD COLUMN localizacao_id UUID REFERENCES localizacao(id);
ALTER TABLE nao_conformidade ADD COLUMN localizacao_id UUID REFERENCES localizacao(id);

-- Migrar dados existentes: criar localizacoes a partir dos textos existentes
-- Para cada localizacao distinta por estabelecimento, cria um registro
INSERT INTO localizacao (id, nome, estabelecimento_id, ativo)
SELECT DISTINCT gen_random_uuid(), d.localizacao, d.estabelecimento_id, true
FROM desvio d
WHERE d.localizacao IS NOT NULL AND d.localizacao != ''
ON CONFLICT DO NOTHING;

INSERT INTO localizacao (id, nome, estabelecimento_id, ativo)
SELECT DISTINCT gen_random_uuid(), nc.localizacao, nc.estabelecimento_id, true
FROM nao_conformidade nc
WHERE nc.localizacao IS NOT NULL AND nc.localizacao != ''
AND NOT EXISTS (
    SELECT 1 FROM localizacao l
    WHERE l.nome = nc.localizacao AND l.estabelecimento_id = nc.estabelecimento_id
);

-- Atualizar desvios com localizacao_id
UPDATE desvio d
SET localizacao_id = l.id
FROM localizacao l
WHERE l.nome = d.localizacao AND l.estabelecimento_id = d.estabelecimento_id;

-- Atualizar nao_conformidades com localizacao_id
UPDATE nao_conformidade nc
SET localizacao_id = l.id
FROM localizacao l
WHERE l.nome = nc.localizacao AND l.estabelecimento_id = nc.estabelecimento_id;

-- Remover coluna localizacao antiga (texto)
ALTER TABLE desvio DROP COLUMN localizacao;
ALTER TABLE nao_conformidade DROP COLUMN localizacao;
