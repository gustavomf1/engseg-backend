CREATE TABLE desvio_email_manual (
    desvio_id UUID NOT NULL REFERENCES desvio(id) ON DELETE CASCADE,
    email     VARCHAR(255) NOT NULL
);
