ALTER TABLE email_padrao
    DROP CONSTRAINT IF EXISTS uk_email_padrao_est_emp_email_tipo;

DROP INDEX IF EXISTS idx_email_padrao_est_emp_tipo;

ALTER TABLE email_padrao DROP COLUMN tipo;

ALTER TABLE email_padrao
    ADD CONSTRAINT uk_email_padrao_est_emp_email
    UNIQUE (estabelecimento_id, empresa_id, email);

CREATE INDEX idx_email_padrao_est_emp
    ON email_padrao(estabelecimento_id, empresa_id);
