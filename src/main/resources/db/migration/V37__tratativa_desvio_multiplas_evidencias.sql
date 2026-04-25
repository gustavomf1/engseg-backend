ALTER TABLE tratativa_desvio DROP COLUMN IF EXISTS evidencia_id;

CREATE TABLE tratativa_desvio_evidencia (
    tratativa_desvio_id UUID NOT NULL REFERENCES tratativa_desvio(id) ON DELETE CASCADE,
    evidencia_id        UUID NOT NULL REFERENCES evidencia(id)        ON DELETE CASCADE,
    PRIMARY KEY (tratativa_desvio_id, evidencia_id)
);
