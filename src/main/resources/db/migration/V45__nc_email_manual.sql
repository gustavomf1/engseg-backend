CREATE TABLE nc_email_manual (
    nc_id UUID NOT NULL REFERENCES nao_conformidade(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL
);
