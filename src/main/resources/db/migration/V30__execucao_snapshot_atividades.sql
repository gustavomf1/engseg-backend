-- Per-activity data in execution snapshots (same pattern as investigacao_snapshot_atividade)
CREATE TABLE execucao_snapshot_atividade (
    snapshot_id UUID NOT NULL REFERENCES execucao_snapshot(id) ON DELETE CASCADE,
    descricao TEXT,
    ordem INTEGER NOT NULL
);
