CREATE TABLE convite_usuario (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    perfil VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    usado CHAR(1) NOT NULL DEFAULT 'N',
    criado_em TIMESTAMP NOT NULL
);
