-- Adiciona título, status e motivo de rejeição às atividades do plano de ação
ALTER TABLE atividade_plano_acao
    ADD COLUMN titulo VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    ADD COLUMN motivo_rejeicao TEXT;

-- Migra registros existentes: usa os primeiros 100 chars da descrição como título
UPDATE atividade_plano_acao SET titulo = LEFT(descricao, 100) WHERE titulo = '';
