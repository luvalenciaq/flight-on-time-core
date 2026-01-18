CREATE TABLE distancias (
    id BIGSERIAL PRIMARY KEY,
    origen_aeropuerto_id BIGINT NOT NULL,
    destino_aeropuerto_id BIGINT NOT NULL,
    distancia_km DOUBLE PRECISION NOT NULL,

    -- Foreign Keys
    CONSTRAINT fk_distancia_origen
        FOREIGN KEY (origen_aeropuerto_id)
        REFERENCES aeropuertos(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_distancia_destino
        FOREIGN KEY (destino_aeropuerto_id)
        REFERENCES aeropuertos(id)
        ON DELETE RESTRICT,

    -- Evita duplicados para la misma ruta
    CONSTRAINT uq_origen_destino
        UNIQUE (origen_aeropuerto_id, destino_aeropuerto_id)
);
