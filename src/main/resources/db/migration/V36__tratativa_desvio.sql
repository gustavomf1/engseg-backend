CREATE TABLE tratativa_desvio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    desvio_id UUID NOT NULL REFERENCES desvio(id) ON DELETE CASCADE,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    evidencia_id UUID REFERENCES evidencia(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    motivo_reprovacao TEXT,
    numero INTEGER NOT NULL,
    dt_criacao TIMESTAMP NOT NULL DEFAULT NOW()
);
