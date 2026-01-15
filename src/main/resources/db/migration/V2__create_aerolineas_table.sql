CREATE TABLE aerolineas (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50),
    nombre VARCHAR(100) NOT NULL,
    pais_origen VARCHAR(50),
    activa BOOLEAN NOT NULL DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);