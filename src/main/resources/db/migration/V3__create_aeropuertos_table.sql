-- Tabla de aeropuertos
CREATE TABLE aeropuertos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(10),
    nombre VARCHAR(100) NOT NULL,
    ciudad VARCHAR(50) NOT NULL,
    pais VARCHAR(50) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION
);