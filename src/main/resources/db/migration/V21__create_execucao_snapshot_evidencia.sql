CREATE TABLE execucao_snapshot_evidencia (
    execucao_snapshot_id UUID NOT NULL REFERENCES execucao_snapshot(id),
    evidencia_id         UUID NOT NULL REFERENCES evidencia(id),
    PRIMARY KEY (execucao_snapshot_id, evidencia_id)
);
