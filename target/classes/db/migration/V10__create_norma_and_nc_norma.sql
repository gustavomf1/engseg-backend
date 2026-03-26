CREATE TABLE norma (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE nao_conformidade_norma (
    nao_conformidade_id UUID NOT NULL REFERENCES nao_conformidade(id),
    norma_id UUID NOT NULL REFERENCES norma(id),
    PRIMARY KEY (nao_conformidade_id, norma_id)
);

ALTER TABLE nao_conformidade ALTER COLUMN nr_relacionada DROP NOT NULL;
