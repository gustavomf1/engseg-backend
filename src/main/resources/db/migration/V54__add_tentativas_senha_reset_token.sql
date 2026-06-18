ALTER TABLE senha_reset_token
    ADD COLUMN tentativas INTEGER NOT NULL DEFAULT 0;
