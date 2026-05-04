ALTER TABLE desvio ADD COLUMN empresa_contratada_id UUID REFERENCES empresa(id);
