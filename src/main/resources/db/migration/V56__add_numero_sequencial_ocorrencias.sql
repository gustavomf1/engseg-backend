-- Código legível (NC-0001, DESV-0001) para não_conformidade e desvio, além do UUID interno.
-- Numeração sequencial global por tipo, backfillada pela ordem de data_registro.

ALTER TABLE nao_conformidade ADD COLUMN numero_sequencial BIGINT;

WITH ordenado AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY data_registro, id) AS rn
    FROM nao_conformidade
)
UPDATE nao_conformidade nc
SET numero_sequencial = ordenado.rn
FROM ordenado
WHERE nc.id = ordenado.id;

CREATE SEQUENCE nao_conformidade_numero_seq OWNED BY nao_conformidade.numero_sequencial;
SELECT setval('nao_conformidade_numero_seq', COALESCE((SELECT MAX(numero_sequencial) FROM nao_conformidade), 0));
ALTER TABLE nao_conformidade ALTER COLUMN numero_sequencial SET DEFAULT nextval('nao_conformidade_numero_seq');
ALTER TABLE nao_conformidade ALTER COLUMN numero_sequencial SET NOT NULL;
ALTER TABLE nao_conformidade ADD CONSTRAINT uq_nao_conformidade_numero_sequencial UNIQUE (numero_sequencial);

ALTER TABLE desvio ADD COLUMN numero_sequencial BIGINT;

WITH ordenado AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY data_registro, id) AS rn
    FROM desvio
)
UPDATE desvio d
SET numero_sequencial = ordenado.rn
FROM ordenado
WHERE d.id = ordenado.id;

CREATE SEQUENCE desvio_numero_seq OWNED BY desvio.numero_sequencial;
SELECT setval('desvio_numero_seq', COALESCE((SELECT MAX(numero_sequencial) FROM desvio), 0));
ALTER TABLE desvio ALTER COLUMN numero_sequencial SET DEFAULT nextval('desvio_numero_seq');
ALTER TABLE desvio ALTER COLUMN numero_sequencial SET NOT NULL;
ALTER TABLE desvio ADD CONSTRAINT uq_desvio_numero_sequencial UNIQUE (numero_sequencial);
