-- =============================================================
-- MIGRACIÓN DE AJUSTE: INSERTAR FALTANTES
-- =============================================================
-- Crear el índice único en la columna codigo

ALTER TABLE aeropuertos
ADD CONSTRAINT unique_codigo_aeropuerto UNIQUE (codigo);

INSERT INTO aeropuertos (codigo, nombre, ciudad, pais, activo) VALUES
('ACV', 'California Redwood Coast-Humboldt County Airport', 'Arcata/Eureka', 'Estados Unidos', true),
('AZA', 'Phoenix-Mesa Gateway Airport', 'Phoenix', 'Estados Unidos', true),
('BLI', 'Bellingham International Airport', 'Bellingham', 'Estados Unidos', true),
('BLV', 'MidAmerica St. Louis Airport', 'Belleville', 'Estados Unidos', true),
('CAK', 'Akron-Canton Airport', 'Akron', 'Estados Unidos', true),
('FNT', 'Bishop International Airport', 'Flint', 'Estados Unidos', true),
('FWA', 'Fort Wayne International Airport', 'Fort Wayne', 'Estados Unidos', true),
('GRI', 'Central Nebraska Regional Airport', 'Grand Island', 'Estados Unidos', true),
('GTF', 'Great Falls International Airport', 'Great Falls', 'Estados Unidos', true),
('HHH', 'Hilton Head Airport', 'Hilton Head Island', 'Estados Unidos', true),
('HLN', 'Helena Regional Airport', 'Helena', 'Estados Unidos', true),
('IAG', 'Niagara Falls International Airport', 'Niagara Falls', 'Estados Unidos', true),
('IDA', 'Idaho Falls Regional Airport', 'Idaho Falls', 'Estados Unidos', true),
('LCK', 'Rickenbacker International Airport', 'Columbus', 'Estados Unidos', true),
('LWS', 'Lewiston-Nez Perce County Airport', 'Lewiston', 'Estados Unidos', true),
('MLB', 'Melbourne Orlando International Airport', 'Melbourne', 'Estados Unidos', true),
('OTH', 'Southwest Oregon Regional Airport', 'North Bend', 'Estados Unidos', true),
('PBG', 'Plattsburgh International Airport', 'Plattsburgh', 'Estados Unidos', true),
('PGD', 'Punta Gorda Airport', 'Punta Gorda', 'Estados Unidos', true),
('PIA', 'General Wayne A. Downing Peoria International Airport', 'Peoria', 'Estados Unidos', true),
('PIE', 'St. Pete–Clearwater International Airport', 'St. Petersburg', 'Estados Unidos', true),
('PSM', 'Portsmouth International Airport at Pease', 'Portsmouth', 'Estados Unidos', true),
('PVU', 'Provo Municipal Airport', 'Provo', 'Estados Unidos', true),
('RDD', 'Redding Regional Airport', 'Redding', 'Estados Unidos', true),
('RFD', 'Chicago Rockford International Airport', 'Rockford', 'Estados Unidos', true),
('SCK', 'Stockton Metropolitan Airport', 'Stockton', 'Estados Unidos', true),
('SFB', 'Orlando Sanford International Airport', 'Sanford', 'Estados Unidos', true),
('SGU', 'St. George Regional Airport', 'St. George', 'Estados Unidos', true),
('SMX', 'Santa Maria Public Airport', 'Santa Maria', 'Estados Unidos', true),
('SUN', 'Friedman Memorial Airport', 'Hailey', 'Estados Unidos', true),
('USA', 'Spartanburg Downtown Memorial Airport', 'Spartanburg', 'Estados Unidos', true),
('XWA', 'Williston Basin International Airport', 'Williston', 'Estados Unidos', true)
ON CONFLICT (codigo) DO NOTHING;

-- =============================================================
--DESACTIVAR AEROPUERTOS QUE NO ESTABAN EN LA LISTA
-- =============================================================
UPDATE aeropuertos
SET activo = false
WHERE codigo NOT IN ('ABE', 'ABQ', 'ACK', 'ACV', 'AGS', 'ALB', 'AMA', 'ANC', 'ATL', 'ATW', 'AUS', 'AVL', 'AVP', 'AZA', 'BDL', 'BFL', 'BGR', 'BHM', 'BIL', 'BIS', 'BLI', 'BLV', 'BNA', 'BOI', 'BOS', 'BRO', 'BTR', 'BTV', 'BUF', 'BUR', 'BWI', 'BZN', 'CAE', 'CAK', 'CHA', 'CHO', 'CHS', 'CID', 'CLE', 'CLT', 'CMH', 'COD', 'COS', 'CPR', 'CRP', 'CVG', 'DAB', 'DAY', 'DCA', 'DEN', 'DFW', 'DLH', 'DRO', 'DSM', 'DTW', 'ECP', 'EGE', 'ELP', 'EUG', 'EWR', 'EYW', 'FAI', 'FAR', 'FAT', 'FCA', 'FLL', 'FNT', 'FSD', 'FWA', 'GEG', 'GFK', 'GJT', 'GPT', 'GRB', 'GRI', 'GRR', 'GSO', 'GSP', 'GTF', 'GUC', 'HDN', 'HHH', 'HLN', 'HNL', 'HOB', 'HOU', 'HPN', 'HRL', 'HSV', 'IAD', 'IAG', 'IAH', 'ICT', 'IDA', 'ILM', 'IND', 'JAC', 'JAN', 'JAX', 'JFK', 'KOA', 'LAS', 'LAX', 'LBB', 'LCH', 'LCK', 'LEX', 'LFT', 'LGA', 'LIH', 'LIT', 'LNK', 'LRD', 'LWS', 'MAF', 'MCI', 'MCO', 'MDT', 'MDW', 'MEM', 'MFE', 'MFR', 'MHT', 'MIA', 'MKE', 'MLB', 'MLI', 'MOB', 'MOT', 'MRY', 'MSN', 'MSO', 'MSP', 'MSY', 'MTJ', 'MVY', 'MYR', 'OAK', 'OGG', 'OKC', 'OMA', 'ONT', 'ORD', 'ORF', 'ORH', 'OTH', 'PBG', 'PBI', 'PDX', 'PGD', 'PHL', 'PHX', 'PIA', 'PIE', 'PIT', 'PNS', 'PSC', 'PSM', 'PSP', 'PVD', 'PVU', 'PWM', 'RAP', 'RDD', 'RDM', 'RDU', 'RFD', 'RIC', 'RNO', 'ROA', 'ROC', 'RST', 'RSW', 'SAF', 'SAN', 'SAT', 'SAV', 'SBA', 'SBN', 'SBP', 'SCE', 'SCK', 'SDF', 'SEA', 'SFB', 'SFO', 'SGF', 'SGU', 'SHV', 'SJC', 'SJU', 'SLC', 'SMF', 'SMX', 'SNA', 'SRQ', 'STL', 'STT', 'SUN', 'SYR', 'TPA', 'TUL', 'TUS', 'TVC', 'TYS', 'USA', 'VPS', 'XNA', 'XWA');