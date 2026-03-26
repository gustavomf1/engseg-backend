ALTER TABLE nao_conformidade ADD COLUMN usuario_criacao_id UUID REFERENCES usuario(id);
ALTER TABLE desvio ADD COLUMN usuario_criacao_id UUID REFERENCES usuario(id);

-- Preencher registros existentes com o tecnico_id como usuario de criação
UPDATE nao_conformidade SET usuario_criacao_id = tecnico_id WHERE tecnico_id IS NOT NULL;
UPDATE desvio SET usuario_criacao_id = tecnico_id WHERE tecnico_id IS NOT NULL;
