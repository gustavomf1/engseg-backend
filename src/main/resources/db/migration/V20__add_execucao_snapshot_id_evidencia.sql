ALTER TABLE evidencia ADD COLUMN execucao_snapshot_id UUID REFERENCES execucao_snapshot(id);
