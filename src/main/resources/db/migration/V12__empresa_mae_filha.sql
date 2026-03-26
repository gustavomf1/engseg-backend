-- Adiciona relacionamento empresa mae/filha (self-referencing)
ALTER TABLE empresa ADD COLUMN empresa_mae_id UUID REFERENCES empresa(id);

-- Tabela de vinculo entre estabelecimentos e empresas filhas (N:N)
CREATE TABLE estabelecimento_empresa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estabelecimento_id UUID NOT NULL REFERENCES estabelecimento(id),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(estabelecimento_id, empresa_id)
);
