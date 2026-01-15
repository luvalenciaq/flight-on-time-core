CREATE TABLE predicciones (
    id BIGSERIAL PRIMARY KEY,
    prevision estado_vuelo NOT NULL,
    probabilidad DOUBLE PRECISION NOT NULL,
    fecha_prediccion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    flight_id BIGINT NOT NULL,

    -- Foreign Key
    CONSTRAINT fk_prediccion_vuelo FOREIGN KEY (flight_id)
        REFERENCES vuelos(id) ON DELETE CASCADE
);