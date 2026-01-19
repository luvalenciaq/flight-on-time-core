-- Eliminar columnas que no se necesitan
ALTER TABLE aerolineas DROP COLUMN IF EXISTS pais_origen;
ALTER TABLE aerolineas DROP COLUMN IF EXISTS fecha_creacion;

-- Limpiar aerolíneas existentes
DELETE FROM aerolineas;

-- Reiniciar secuencia
ALTER SEQUENCE aerolineas_id_seq RESTART WITH 1;

-- Insertar las 7 aerolíneas del nuevo modelo
INSERT INTO aerolineas (codigo, nombre, activa) VALUES
('G4', 'Allegiant Air', TRUE),
('C5', 'CommuteAir (CommuteAir LLC)', TRUE),
('DL', 'Delta Air Lines Inc.', TRUE),
('YX', 'Republic Airways Inc.', TRUE),
('AA', 'American Airlines, Inc.', TRUE),
('OO', 'SkyWest Airlines', TRUE),
('UA', 'United Airlines', TRUE);