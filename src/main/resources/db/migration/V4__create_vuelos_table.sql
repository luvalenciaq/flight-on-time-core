CREATE TABLE vuelos (
    id BIGSERIAL PRIMARY KEY,
    codigo_aerolinea BIGINT NOT NULL,
    codigo_aeropuerto_origen BIGINT NOT NULL,
    codigo_aeropuerto_destino BIGINT NOT NULL,
    fecha_partida TIMESTAMP NOT NULL,
    distancia_km DOUBLE PRECISION NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_vuelo_aerolinea FOREIGN KEY (codigo_aerolinea)
        REFERENCES aerolineas(id) ON DELETE RESTRICT,
    CONSTRAINT fk_vuelo_origen FOREIGN KEY (codigo_aeropuerto_origen)
        REFERENCES aeropuertos(id) ON DELETE RESTRICT,
    CONSTRAINT fk_vuelo_destino FOREIGN KEY (codigo_aeropuerto_destino)
        REFERENCES aeropuertos(id) ON DELETE RESTRICT
);