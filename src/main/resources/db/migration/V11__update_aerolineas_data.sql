-- 1. Desactivar aerolíneas que ya no están en la lista activa
UPDATE aerolineas
SET activa = false
WHERE codigo IN ('9E', 'AS', 'B6');

-- 2. Insertar las nuevas aerolíneas
INSERT INTO aerolineas (codigo, nombre, pais_origen, activa) VALUES
('G4', 'Allegiant Air', 'Estados Unidos', true),
('DL', 'Delta Air Lines', 'Estados Unidos', true),
('YX', 'Republic Airways', 'Estados Unidos', true),
('OO', 'SkyWest Airlines', 'Estados Unidos', true),
('UA', 'United Airlines', 'Estados Unidos', true)