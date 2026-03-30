CREATE TABLE nc_trecho_norma (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nao_conformidade_id UUID NOT NULL REFERENCES nao_conformidade(id) ON DELETE CASCADE,
    norma_id UUID NOT NULL REFERENCES norma(id),
    clausula_referencia VARCHAR(100),
    texto_editado TEXT NOT NULL,
    data_vinculo TIMESTAMP NOT NULL DEFAULT now()
);
