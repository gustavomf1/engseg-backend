-- Campos de investigação (5 Porquês e Causa Raiz) e descrição de execução
ALTER TABLE nao_conformidade ADD COLUMN IF NOT EXISTS porque_um TEXT;
ALTER TABLE nao_conformidade ADD COLUMN IF NOT EXISTS porque_dois TEXT;
ALTER TABLE nao_conformidade ADD COLUMN IF NOT EXISTS porque_tres TEXT;
ALTER TABLE nao_conformidade ADD COLUMN IF NOT EXISTS porque_quatro TEXT;
ALTER TABLE nao_conformidade ADD COLUMN IF NOT EXISTS porque_cinco TEXT;
ALTER TABLE nao_conformidade ADD COLUMN IF NOT EXISTS causa_raiz TEXT;
ALTER TABLE nao_conformidade ADD COLUMN IF NOT EXISTS descricao_execucao TEXT;

-- Tabela de atividades do plano de ação (preenchidas pelo Externo junto com a investigação)
CREATE TABLE atividade_plano_acao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nao_conformidade_id UUID NOT NULL REFERENCES nao_conformidade(id),
    descricao TEXT NOT NULL,
    ordem INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_atividade_plano_acao_nc_id ON atividade_plano_acao(nao_conformidade_id);

-- Tabela de histórico de decisões (aprovações e rejeições pelo Engenheiro)
CREATE TABLE historico_nc (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nao_conformidade_id UUID NOT NULL REFERENCES nao_conformidade(id),
    usuario_id UUID REFERENCES usuario(id),
    acao VARCHAR(50) NOT NULL,
    comentario TEXT,
    status_anterior VARCHAR(50),
    status_atual VARCHAR(50),
    data_acao TIMESTAMP NOT NULL
);

CREATE INDEX idx_historico_nc_nc_id ON historico_nc(nao_conformidade_id);
