-- V41__unificar_email_padrao.sql
ALTER TABLE email_padrao_nc RENAME TO email_padrao;

ALTER TABLE email_padrao
    ADD COLUMN tipo VARCHAR(20) NOT NULL DEFAULT 'NC';

ALTER TABLE email_padrao
    DROP CONSTRAINT uk_email_padrao_nc_est_emp_email;

ALTER TABLE email_padrao
    ADD CONSTRAINT uk_email_padrao_est_emp_email_tipo
    UNIQUE (estabelecimento_id, empresa_id, email, tipo);

DROP INDEX IF EXISTS idx_email_padrao_nc_est_emp;

CREATE INDEX idx_email_padrao_est_emp_tipo
    ON email_padrao(estabelecimento_id, empresa_id, tipo);
