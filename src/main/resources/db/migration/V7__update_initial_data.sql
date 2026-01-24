-- ============================================
-- 1. Agregar constraint de código único
-- ============================================
ALTER TABLE aeropuertos
ADD CONSTRAINT unique_codigo_aeropuerto UNIQUE (codigo);

-- ============================================
-- 2. Insertar nuevos aeropuertos con coordenadas
-- ============================================
INSERT INTO aeropuertos (codigo, nombre, ciudad, pais, activo, latitud, longitud) VALUES
('ACV', 'California Redwood Coast-Humboldt County Airport', 'Arcata/Eureka', 'Estados Unidos', true, 40.9781, -124.1086),
('AZA', 'Phoenix-Mesa Gateway Airport', 'Phoenix', 'Estados Unidos', true, 33.3078, -111.6555),
('BLI', 'Bellingham International Airport', 'Bellingham', 'Estados Unidos', true, 48.7928, -122.5375),
('BLV', 'MidAmerica St. Louis Airport', 'Belleville', 'Estados Unidos', true, 38.5452, -89.8352),
('CAK', 'Akron-Canton Airport', 'Akron', 'Estados Unidos', true, 40.9161, -81.4422),
('FNT', 'Bishop International Airport', 'Flint', 'Estados Unidos', true, 42.9654, -83.7436),
('FWA', 'Fort Wayne International Airport', 'Fort Wayne', 'Estados Unidos', true, 40.9785, -85.1951),
('GRI', 'Central Nebraska Regional Airport', 'Grand Island', 'Estados Unidos', true, 40.9675, -98.3096),
('GTF', 'Great Falls International Airport', 'Great Falls', 'Estados Unidos', true, 47.4820, -111.3705),
('HHH', 'Hilton Head Airport', 'Hilton Head Island', 'Estados Unidos', true, 32.2244, -80.6975),
('HLN', 'Helena Regional Airport', 'Helena', 'Estados Unidos', true, 46.6068, -111.9828),
('IAG', 'Niagara Falls International Airport', 'Niagara Falls', 'Estados Unidos', true, 43.1073, -78.9462),
('IDA', 'Idaho Falls Regional Airport', 'Idaho Falls', 'Estados Unidos', true, 43.5146, -112.0707),
('LCK', 'Rickenbacker International Airport', 'Columbus', 'Estados Unidos', true, 39.8138, -82.9278),
('LWS', 'Lewiston-Nez Perce County Airport', 'Lewiston', 'Estados Unidos', true, 46.3745, -117.0154),
('MLB', 'Melbourne Orlando International Airport', 'Melbourne', 'Estados Unidos', true, 28.1028, -80.6453),
('OTH', 'Southwest Oregon Regional Airport', 'North Bend', 'Estados Unidos', true, 43.4171, -124.2460),
('PBG', 'Plattsburgh International Airport', 'Plattsburgh', 'Estados Unidos', true, 44.6509, -73.4681),
('PGD', 'Punta Gorda Airport', 'Punta Gorda', 'Estados Unidos', true, 26.9202, -81.9905),
('PIA', 'General Wayne A. Downing Peoria International Airport', 'Peoria', 'Estados Unidos', true, 40.6642, -89.6932),
('PIE', 'St. Pete–Clearwater International Airport', 'St. Petersburg', 'Estados Unidos', true, 27.9102, -82.6874),
('PSM', 'Portsmouth International Airport at Pease', 'Portsmouth', 'Estados Unidos', true, 43.0779, -70.8233),
('PVU', 'Provo Municipal Airport', 'Provo', 'Estados Unidos', true, 40.2192, -111.7233),
('RDD', 'Redding Regional Airport', 'Redding', 'Estados Unidos', true, 40.5090, -122.2934),
('RFD', 'Chicago Rockford International Airport', 'Rockford', 'Estados Unidos', true, 42.1954, -89.0972),
('SCK', 'Stockton Metropolitan Airport', 'Stockton', 'Estados Unidos', true, 37.8942, -121.2385),
('SFB', 'Orlando Sanford International Airport', 'Sanford', 'Estados Unidos', true, 28.7776, -81.2375),
('SGU', 'St. George Regional Airport', 'St. George', 'Estados Unidos', true, 37.0364, -113.5103),
('SMX', 'Santa Maria Public Airport', 'Santa Maria', 'Estados Unidos', true, 34.8989, -120.4575),
('SUN', 'Friedman Memorial Airport', 'Hailey', 'Estados Unidos', true, 43.5048, -114.2961),
('USA', 'Spartanburg Downtown Memorial Airport', 'Spartanburg', 'Estados Unidos', true, 34.9126, -81.9564),
('XWA', 'Williston Basin International Airport', 'Williston', 'Estados Unidos', true, 48.1778, -103.6422);

-- ============================================
-- 3. Actualizar aerolíneas
-- ============================================

-- Desactivar aerolíneas que ya no están en la lista activa
UPDATE aerolineas
SET activa = false
WHERE codigo IN ('9E', 'AS', 'B6');

-- Insertar las nuevas aerolíneas
INSERT INTO aerolineas (codigo, nombre, pais_origen, activa) VALUES
('G4', 'Allegiant Air', 'Estados Unidos', true),
('DL', 'Delta Air Lines', 'Estados Unidos', true),
('YX', 'Republic Airways', 'Estados Unidos', true),
('OO', 'SkyWest Airlines', 'Estados Unidos', true),
('UA', 'United Airlines', 'Estados Unidos', true);

