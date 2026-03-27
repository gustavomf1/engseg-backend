ALTER TABLE nao_conformidade
    ADD COLUMN reincidencia VARCHAR(1) NOT NULL DEFAULT 'N',
    ADD COLUMN nc_anterior_id UUID REFERENCES nao_conformidade(id);
