CREATE TABLE email_padrao_nc (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estabelecimento_id  UUID NOT NULL REFERENCES estabelecimento(id) ON DELETE CASCADE,
    empresa_id          UUID NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    email               VARCHAR(255) NOT NULL,
    descricao           VARCHAR(255)
);

CREATE INDEX idx_email_padrao_nc_est_emp
    ON email_padrao_nc(estabelecimento_id, empresa_id);

ALTER TABLE email_padrao_nc
    ADD CONSTRAINT uk_email_padrao_nc_est_emp_email
    UNIQUE (estabelecimento_id, empresa_id, email);
