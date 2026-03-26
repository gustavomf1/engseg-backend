ALTER TABLE nao_conformidade ADD COLUMN vencida VARCHAR(1) NOT NULL DEFAULT 'N';

-- Marca como vencidas as NCs que já passaram do prazo e não foram concluídas
UPDATE nao_conformidade
SET vencida = 'S'
WHERE data_limite_resolucao < CURRENT_DATE
  AND status NOT IN ('CONCLUIDO');
