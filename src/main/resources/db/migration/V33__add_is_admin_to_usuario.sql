ALTER TABLE usuario ADD COLUMN is_admin CHAR(1) NOT NULL DEFAULT 'N'
  CHECK (is_admin IN ('S', 'N'));
