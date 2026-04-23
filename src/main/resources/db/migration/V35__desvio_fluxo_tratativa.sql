-- Novos campos na tabela desvio para suportar o fluxo de tratativa
ALTER TABLE desvio ADD COLUMN responsavel_desvio_id UUID REFERENCES usuario(id);
ALTER TABLE desvio ADD COLUMN responsavel_tratativa_id UUID REFERENCES usuario(id);
ALTER TABLE desvio ADD COLUMN observacao_tratativa TEXT;
ALTER TABLE desvio ADD COLUMN evidencia_tratativa_id UUID REFERENCES evidencia(id);

-- Tabela de histórico do desvio (auditoria imutável)
CREATE TABLE historico_desvio (
    id UUID PRIMARY KEY,
    desvio_id UUID NOT NULL REFERENCES desvio(id) ON DELETE CASCADE,
    usuario_id UUID REFERENCES usuario(id),
    tipo VARCHAR(30) NOT NULL,
    comentario TEXT,
    status_anterior VARCHAR(30),
    status_atual VARCHAR(30),
    snapshot_observacao TEXT,
    snapshot_evidencia_id UUID, -- sem FK intencional: preserva referência mesmo se evidência for deletada
    data_acao TIMESTAMP NOT NULL
);
