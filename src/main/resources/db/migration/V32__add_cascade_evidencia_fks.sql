-- Adiciona ON DELETE CASCADE nas FKs da tabela evidencia que faltaram na V31

-- evidencia -> atividade_plano_acao
ALTER TABLE evidencia DROP CONSTRAINT IF EXISTS evidencia_atividade_plano_acao_id_fkey;
ALTER TABLE evidencia ADD CONSTRAINT evidencia_atividade_plano_acao_id_fkey
    FOREIGN KEY (atividade_plano_acao_id) REFERENCES atividade_plano_acao(id) ON DELETE CASCADE;

-- evidencia -> execucao_snapshot
ALTER TABLE evidencia DROP CONSTRAINT IF EXISTS evidencia_execucao_snapshot_id_fkey;
ALTER TABLE evidencia ADD CONSTRAINT evidencia_execucao_snapshot_id_fkey
    FOREIGN KEY (execucao_snapshot_id) REFERENCES execucao_snapshot(id) ON DELETE CASCADE;

-- evidencia -> desvio
ALTER TABLE evidencia DROP CONSTRAINT IF EXISTS fk_evidencia_desvio;
ALTER TABLE evidencia ADD CONSTRAINT fk_evidencia_desvio
    FOREIGN KEY (desvio_id) REFERENCES desvio(id) ON DELETE CASCADE;
