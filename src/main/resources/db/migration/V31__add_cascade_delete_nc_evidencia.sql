-- Adiciona ON DELETE CASCADE em todas as FKs que referenciam nao_conformidade(id)
-- para permitir exclusão da NC sem violar constraints

-- devolutiva
ALTER TABLE devolutiva DROP CONSTRAINT IF EXISTS devolutiva_nao_conformidade_id_fkey;
ALTER TABLE devolutiva ADD CONSTRAINT devolutiva_nao_conformidade_id_fkey
    FOREIGN KEY (nao_conformidade_id) REFERENCES nao_conformidade(id) ON DELETE CASCADE;

-- execucao_acao
ALTER TABLE execucao_acao DROP CONSTRAINT IF EXISTS execucao_acao_nao_conformidade_id_fkey;
ALTER TABLE execucao_acao ADD CONSTRAINT execucao_acao_nao_conformidade_id_fkey
    FOREIGN KEY (nao_conformidade_id) REFERENCES nao_conformidade(id) ON DELETE CASCADE;

-- validacao
ALTER TABLE validacao DROP CONSTRAINT IF EXISTS validacao_nao_conformidade_id_fkey;
ALTER TABLE validacao ADD CONSTRAINT validacao_nao_conformidade_id_fkey
    FOREIGN KEY (nao_conformidade_id) REFERENCES nao_conformidade(id) ON DELETE CASCADE;

-- evidencia (nao_conformidade_id)
ALTER TABLE evidencia DROP CONSTRAINT IF EXISTS evidencia_nao_conformidade_id_fkey;
ALTER TABLE evidencia ADD CONSTRAINT evidencia_nao_conformidade_id_fkey
    FOREIGN KEY (nao_conformidade_id) REFERENCES nao_conformidade(id) ON DELETE CASCADE;

-- evidencia (execucao_acao_id) — cascade quando execucao_acao é deletada
ALTER TABLE evidencia DROP CONSTRAINT IF EXISTS evidencia_execucao_acao_id_fkey;
ALTER TABLE evidencia ADD CONSTRAINT evidencia_execucao_acao_id_fkey
    FOREIGN KEY (execucao_acao_id) REFERENCES execucao_acao(id) ON DELETE CASCADE;

-- nao_conformidade_norma (junction ManyToMany)
ALTER TABLE nao_conformidade_norma DROP CONSTRAINT IF EXISTS nao_conformidade_norma_nao_conformidade_id_fkey;
ALTER TABLE nao_conformidade_norma ADD CONSTRAINT nao_conformidade_norma_nao_conformidade_id_fkey
    FOREIGN KEY (nao_conformidade_id) REFERENCES nao_conformidade(id) ON DELETE CASCADE;

-- atividade_plano_acao
ALTER TABLE atividade_plano_acao DROP CONSTRAINT IF EXISTS atividade_plano_acao_nao_conformidade_id_fkey;
ALTER TABLE atividade_plano_acao ADD CONSTRAINT atividade_plano_acao_nao_conformidade_id_fkey
    FOREIGN KEY (nao_conformidade_id) REFERENCES nao_conformidade(id) ON DELETE CASCADE;

-- historico_nc
ALTER TABLE historico_nc DROP CONSTRAINT IF EXISTS historico_nc_nao_conformidade_id_fkey;
ALTER TABLE historico_nc ADD CONSTRAINT historico_nc_nao_conformidade_id_fkey
    FOREIGN KEY (nao_conformidade_id) REFERENCES nao_conformidade(id) ON DELETE CASCADE;

-- investigacao_snapshot
ALTER TABLE investigacao_snapshot DROP CONSTRAINT IF EXISTS investigacao_snapshot_nao_conformidade_id_fkey;
ALTER TABLE investigacao_snapshot ADD CONSTRAINT investigacao_snapshot_nao_conformidade_id_fkey
    FOREIGN KEY (nao_conformidade_id) REFERENCES nao_conformidade(id) ON DELETE CASCADE;

-- execucao_snapshot
ALTER TABLE execucao_snapshot DROP CONSTRAINT IF EXISTS execucao_snapshot_nao_conformidade_id_fkey;
ALTER TABLE execucao_snapshot ADD CONSTRAINT execucao_snapshot_nao_conformidade_id_fkey
    FOREIGN KEY (nao_conformidade_id) REFERENCES nao_conformidade(id) ON DELETE CASCADE;

-- execucao_snapshot_evidencia (junction — cascade em ambas as direções)
ALTER TABLE execucao_snapshot_evidencia DROP CONSTRAINT IF EXISTS execucao_snapshot_evidencia_execucao_snapshot_id_fkey;
ALTER TABLE execucao_snapshot_evidencia ADD CONSTRAINT execucao_snapshot_evidencia_execucao_snapshot_id_fkey
    FOREIGN KEY (execucao_snapshot_id) REFERENCES execucao_snapshot(id) ON DELETE CASCADE;

ALTER TABLE execucao_snapshot_evidencia DROP CONSTRAINT IF EXISTS execucao_snapshot_evidencia_evidencia_id_fkey;
ALTER TABLE execucao_snapshot_evidencia ADD CONSTRAINT execucao_snapshot_evidencia_evidencia_id_fkey
    FOREIGN KEY (evidencia_id) REFERENCES evidencia(id) ON DELETE CASCADE;

-- execucao_snapshot_atividade (collection table)
ALTER TABLE execucao_snapshot_atividade DROP CONSTRAINT IF EXISTS execucao_snapshot_atividade_snapshot_id_fkey;
ALTER TABLE execucao_snapshot_atividade ADD CONSTRAINT execucao_snapshot_atividade_snapshot_id_fkey
    FOREIGN KEY (snapshot_id) REFERENCES execucao_snapshot(id) ON DELETE CASCADE;

-- nc_anterior_id (self-reference) — SET NULL ao deletar NC referenciada
ALTER TABLE nao_conformidade DROP CONSTRAINT IF EXISTS nao_conformidade_nc_anterior_id_fkey;
ALTER TABLE nao_conformidade ADD CONSTRAINT nao_conformidade_nc_anterior_id_fkey
    FOREIGN KEY (nc_anterior_id) REFERENCES nao_conformidade(id) ON DELETE SET NULL;
