-- Execução por atividade: cada atividade tem sua própria execução, status e motivo
ALTER TABLE atividade_plano_acao
    ADD COLUMN descricao_execucao TEXT,
    ADD COLUMN status_execucao VARCHAR(20),
    ADD COLUMN motivo_rejeicao_execucao TEXT;

-- Evidência pode ser vinculada a uma atividade específica
ALTER TABLE evidencia
    ADD COLUMN atividade_plano_acao_id UUID REFERENCES atividade_plano_acao(id);
