CREATE TABLE investigacao_snapshot (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nao_conformidade_id UUID NOT NULL REFERENCES nao_conformidade(id),
    porque_um TEXT,
    porque_um_resposta TEXT,
    porque_dois TEXT,
    porque_dois_resposta TEXT,
    porque_tres TEXT,
    porque_tres_resposta TEXT,
    porque_quatro TEXT,
    porque_quatro_resposta TEXT,
    porque_cinco TEXT,
    porque_cinco_resposta TEXT,
    causa_raiz TEXT,
    data_submissao TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    comentario_revisao TEXT
);

CREATE TABLE investigacao_snapshot_atividade (
    snapshot_id UUID NOT NULL REFERENCES investigacao_snapshot(id) ON DELETE CASCADE,
    descricao TEXT NOT NULL,
    ordem INTEGER NOT NULL
);

CREATE TABLE execucao_snapshot (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nao_conformidade_id UUID NOT NULL REFERENCES nao_conformidade(id),
    descricao_execucao TEXT NOT NULL,
    data_submissao TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    comentario_revisao TEXT
);
