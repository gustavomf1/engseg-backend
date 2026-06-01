CREATE TABLE senha_reset_token (
    id                       UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id               UUID        NOT NULL REFERENCES usuario(id),
    otp                      VARCHAR(6)  NOT NULL,
    otp_expires_at           TIMESTAMP   NOT NULL,
    reset_token              UUID,
    reset_token_expires_at   TIMESTAMP,
    usado                    CHAR(1)     NOT NULL DEFAULT 'N',
    created_at               TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE INDEX idx_srt_usuario     ON senha_reset_token(usuario_id);
CREATE INDEX idx_srt_reset_token ON senha_reset_token(reset_token)
    WHERE reset_token IS NOT NULL;
