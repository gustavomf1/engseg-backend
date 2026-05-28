ALTER TABLE norma
  ADD COLUMN criado_em         TIMESTAMP NOT NULL DEFAULT NOW(),
  ADD COLUMN criado_por_id     UUID      REFERENCES usuario(id) ON DELETE SET NULL,
  ADD COLUMN atualizado_em     TIMESTAMP NOT NULL DEFAULT NOW(),
  ADD COLUMN atualizado_por_id UUID      REFERENCES usuario(id) ON DELETE SET NULL;
