ALTER TABLE evidencia ADD COLUMN desvio_id UUID;

ALTER TABLE evidencia ADD CONSTRAINT fk_evidencia_desvio
    FOREIGN KEY (desvio_id) REFERENCES desvio(id);
