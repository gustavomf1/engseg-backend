ALTER TABLE usuario ADD COLUMN is_admin VARCHAR(1) NOT NULL DEFAULT 'N'
  CHECK (is_admin IN ('S', 'N'));
