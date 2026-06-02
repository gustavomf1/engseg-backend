ALTER TABLE nao_conformidade ADD COLUMN empresa_contratada_id UUID REFERENCES empresa(id);
