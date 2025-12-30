package com.flightontime.core.service;

import com.flightontime.core.dto.FlightRequestDTO;
import com.flightontime.core.model.Flight;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
/**
 * Servicio de Ingeniería de Características.
 * <p>
 * Este servicio actúa como un puente entre los datos de negocio y el modelo de
 * IA.
 * Se encarga de transformar un objeto de solicitud (humano-legible) en un
 * vector numérico que el modelo ONNX puede entender.
 * </p>
 */
public class FeatureEngineeringService {
    /**
     * Lista de aerolíneas soportadas por el modelo.
     * <p>
     * IMPORTANTE: El orden de esta lista es CRÍTICO. Debe coincidir exactamente
     * con el orden utilizado durante el entrenamiento del modelo para que el
     * One-Hot Encoding funcione correctamente.
     * Índices: 0="9E", 1="AA", 2="AS", 3="B6", 4="C5"
     * </p>
     */
    private static final List<String> AEROLINEAS_CONOCIDAS = List.of("9E", "AA", "AS", "B6", "C5");
    /**
     * Mapa para Label Encoding de aeropuertos.
     * Asigna un valor numérico único (ID) a cada código IATA de aeropuerto.
     * Esto evita crear cientos de columnas para cada aeropuerto.
     */
    private static final Map<String, Float> AEROPUERTOS_MAP = new HashMap<>();

    static {
        // Inicialización estática del mapa grande
        AEROPUERTOS_MAP.put("ABE", 0.0f); AEROPUERTOS_MAP.put("ABQ", 1.0f); AEROPUERTOS_MAP.put("ABY", 2.0f);
        AEROPUERTOS_MAP.put("ACK", 3.0f); AEROPUERTOS_MAP.put("ADK", 4.0f); AEROPUERTOS_MAP.put("ADQ", 5.0f);
        AEROPUERTOS_MAP.put("AEX", 6.0f); AEROPUERTOS_MAP.put("AGS", 7.0f); AEROPUERTOS_MAP.put("AKN", 8.0f);
        AEROPUERTOS_MAP.put("ALB", 9.0f); AEROPUERTOS_MAP.put("AMA", 10.0f); AEROPUERTOS_MAP.put("ANC", 11.0f);
        AEROPUERTOS_MAP.put("ATL", 12.0f); AEROPUERTOS_MAP.put("ATW", 13.0f); AEROPUERTOS_MAP.put("AUS", 14.0f);
        AEROPUERTOS_MAP.put("AVL", 15.0f); AEROPUERTOS_MAP.put("AVP", 16.0f); AEROPUERTOS_MAP.put("AZO", 17.0f);
        AEROPUERTOS_MAP.put("BDL", 18.0f); AEROPUERTOS_MAP.put("BET", 19.0f); AEROPUERTOS_MAP.put("BFL", 20.0f);
        AEROPUERTOS_MAP.put("BGM", 21.0f); AEROPUERTOS_MAP.put("BGR", 22.0f); AEROPUERTOS_MAP.put("BHM", 23.0f);
        AEROPUERTOS_MAP.put("BIL", 24.0f); AEROPUERTOS_MAP.put("BIS", 25.0f); AEROPUERTOS_MAP.put("BMI", 26.0f);
        AEROPUERTOS_MAP.put("BNA", 27.0f); AEROPUERTOS_MAP.put("BOI", 28.0f); AEROPUERTOS_MAP.put("BOS", 29.0f);
        AEROPUERTOS_MAP.put("BQK", 30.0f); AEROPUERTOS_MAP.put("BQN", 31.0f); AEROPUERTOS_MAP.put("BRO", 32.0f);
        AEROPUERTOS_MAP.put("BRW", 33.0f); AEROPUERTOS_MAP.put("BTR", 34.0f); AEROPUERTOS_MAP.put("BTV", 35.0f);
        AEROPUERTOS_MAP.put("BUF", 36.0f); AEROPUERTOS_MAP.put("BUR", 37.0f); AEROPUERTOS_MAP.put("BWI", 38.0f);
        AEROPUERTOS_MAP.put("BZN", 39.0f); AEROPUERTOS_MAP.put("CAE", 40.0f); AEROPUERTOS_MAP.put("CDV", 41.0f);
        AEROPUERTOS_MAP.put("CHA", 42.0f); AEROPUERTOS_MAP.put("CHO", 43.0f); AEROPUERTOS_MAP.put("CHS", 44.0f);
        AEROPUERTOS_MAP.put("CID", 45.0f); AEROPUERTOS_MAP.put("CLE", 46.0f); AEROPUERTOS_MAP.put("CLL", 47.0f);
        AEROPUERTOS_MAP.put("CLT", 48.0f); AEROPUERTOS_MAP.put("CMH", 49.0f); AEROPUERTOS_MAP.put("COD", 50.0f);
        AEROPUERTOS_MAP.put("COS", 51.0f); AEROPUERTOS_MAP.put("CPR", 52.0f); AEROPUERTOS_MAP.put("CRP", 53.0f);
        AEROPUERTOS_MAP.put("CRW", 54.0f); AEROPUERTOS_MAP.put("CSG", 55.0f); AEROPUERTOS_MAP.put("CVG", 56.0f);
        AEROPUERTOS_MAP.put("CWA", 57.0f); AEROPUERTOS_MAP.put("DAB", 58.0f); AEROPUERTOS_MAP.put("DAL", 59.0f);
        AEROPUERTOS_MAP.put("DAY", 60.0f); AEROPUERTOS_MAP.put("DCA", 61.0f); AEROPUERTOS_MAP.put("DEN", 62.0f);
        AEROPUERTOS_MAP.put("DFW", 63.0f); AEROPUERTOS_MAP.put("DHN", 64.0f); AEROPUERTOS_MAP.put("DIK", 65.0f);
        AEROPUERTOS_MAP.put("DLG", 66.0f); AEROPUERTOS_MAP.put("DLH", 67.0f); AEROPUERTOS_MAP.put("DRO", 68.0f);
        AEROPUERTOS_MAP.put("DSM", 69.0f); AEROPUERTOS_MAP.put("DTW", 70.0f); AEROPUERTOS_MAP.put("ECP", 71.0f);
        AEROPUERTOS_MAP.put("EGE", 72.0f); AEROPUERTOS_MAP.put("ELP", 73.0f); AEROPUERTOS_MAP.put("EUG", 74.0f);
        AEROPUERTOS_MAP.put("EVV", 75.0f); AEROPUERTOS_MAP.put("EWR", 76.0f); AEROPUERTOS_MAP.put("EYW", 77.0f);
        AEROPUERTOS_MAP.put("FAI", 78.0f); AEROPUERTOS_MAP.put("FAR", 79.0f); AEROPUERTOS_MAP.put("FAT", 80.0f);
        AEROPUERTOS_MAP.put("FAY", 81.0f); AEROPUERTOS_MAP.put("FCA", 82.0f); AEROPUERTOS_MAP.put("FLG", 83.0f);
        AEROPUERTOS_MAP.put("FLL", 84.0f); AEROPUERTOS_MAP.put("FSD", 85.0f); AEROPUERTOS_MAP.put("FWA", 86.0f);
        AEROPUERTOS_MAP.put("GEG", 87.0f); AEROPUERTOS_MAP.put("GFK", 88.0f); AEROPUERTOS_MAP.put("GJT", 89.0f);
        AEROPUERTOS_MAP.put("GNV", 90.0f); AEROPUERTOS_MAP.put("GPT", 91.0f); AEROPUERTOS_MAP.put("GRB", 92.0f);
        AEROPUERTOS_MAP.put("GRR", 93.0f); AEROPUERTOS_MAP.put("GSO", 94.0f); AEROPUERTOS_MAP.put("GSP", 95.0f);
        AEROPUERTOS_MAP.put("GST", 96.0f); AEROPUERTOS_MAP.put("GTR", 97.0f); AEROPUERTOS_MAP.put("GUC", 98.0f);
        AEROPUERTOS_MAP.put("HDN", 99.0f); AEROPUERTOS_MAP.put("HLN", 100.0f); AEROPUERTOS_MAP.put("HNL", 101.0f);
        AEROPUERTOS_MAP.put("HOB", 102.0f); AEROPUERTOS_MAP.put("HPN", 103.0f); AEROPUERTOS_MAP.put("HRL", 104.0f);
        AEROPUERTOS_MAP.put("HSV", 105.0f); AEROPUERTOS_MAP.put("HYA", 106.0f); AEROPUERTOS_MAP.put("IAD", 107.0f);
        AEROPUERTOS_MAP.put("IAH", 108.0f); AEROPUERTOS_MAP.put("ICT", 109.0f); AEROPUERTOS_MAP.put("IDA", 110.0f);
        AEROPUERTOS_MAP.put("ILM", 111.0f); AEROPUERTOS_MAP.put("IND", 112.0f); AEROPUERTOS_MAP.put("ITH", 113.0f);
        AEROPUERTOS_MAP.put("JAC", 114.0f); AEROPUERTOS_MAP.put("JAN", 115.0f); AEROPUERTOS_MAP.put("JAX", 116.0f);
        AEROPUERTOS_MAP.put("JFK", 117.0f); AEROPUERTOS_MAP.put("JNU", 118.0f); AEROPUERTOS_MAP.put("KOA", 119.0f);
        AEROPUERTOS_MAP.put("KTN", 120.0f); AEROPUERTOS_MAP.put("LAN", 121.0f); AEROPUERTOS_MAP.put("LAS", 122.0f);
        AEROPUERTOS_MAP.put("LAX", 123.0f); AEROPUERTOS_MAP.put("LBB", 124.0f); AEROPUERTOS_MAP.put("LCH", 125.0f);
        AEROPUERTOS_MAP.put("LEX", 126.0f); AEROPUERTOS_MAP.put("LFT", 127.0f); AEROPUERTOS_MAP.put("LGA", 128.0f);
        AEROPUERTOS_MAP.put("LIH", 129.0f); AEROPUERTOS_MAP.put("LIT", 130.0f); AEROPUERTOS_MAP.put("LNK", 131.0f);
        AEROPUERTOS_MAP.put("LRD", 132.0f); AEROPUERTOS_MAP.put("LSE", 133.0f); AEROPUERTOS_MAP.put("MAF", 134.0f);
        AEROPUERTOS_MAP.put("MBS", 135.0f); AEROPUERTOS_MAP.put("MCI", 136.0f); AEROPUERTOS_MAP.put("MCO", 137.0f);
        AEROPUERTOS_MAP.put("MDT", 138.0f); AEROPUERTOS_MAP.put("MDW", 139.0f); AEROPUERTOS_MAP.put("MEM", 140.0f);
        AEROPUERTOS_MAP.put("MFE", 141.0f); AEROPUERTOS_MAP.put("MGM", 142.0f); AEROPUERTOS_MAP.put("MHT", 143.0f);
        AEROPUERTOS_MAP.put("MIA", 144.0f); AEROPUERTOS_MAP.put("MKE", 145.0f); AEROPUERTOS_MAP.put("MLI", 146.0f);
        AEROPUERTOS_MAP.put("MLU", 147.0f); AEROPUERTOS_MAP.put("MOB", 148.0f); AEROPUERTOS_MAP.put("MOT", 149.0f);
        AEROPUERTOS_MAP.put("MQT", 150.0f); AEROPUERTOS_MAP.put("MRY", 151.0f); AEROPUERTOS_MAP.put("MSN", 152.0f);
        AEROPUERTOS_MAP.put("MSO", 153.0f); AEROPUERTOS_MAP.put("MSP", 154.0f); AEROPUERTOS_MAP.put("MSY", 155.0f);
        AEROPUERTOS_MAP.put("MTJ", 156.0f); AEROPUERTOS_MAP.put("MVY", 157.0f); AEROPUERTOS_MAP.put("MYR", 158.0f);
        AEROPUERTOS_MAP.put("OAJ", 159.0f); AEROPUERTOS_MAP.put("OAK", 160.0f); AEROPUERTOS_MAP.put("OGG", 161.0f);
        AEROPUERTOS_MAP.put("OKC", 162.0f); AEROPUERTOS_MAP.put("OMA", 163.0f); AEROPUERTOS_MAP.put("OME", 164.0f);
        AEROPUERTOS_MAP.put("ONT", 165.0f); AEROPUERTOS_MAP.put("ORD", 166.0f); AEROPUERTOS_MAP.put("ORF", 167.0f);
        AEROPUERTOS_MAP.put("ORH", 168.0f); AEROPUERTOS_MAP.put("OTZ", 169.0f); AEROPUERTOS_MAP.put("PAE", 170.0f);
        AEROPUERTOS_MAP.put("PBI", 171.0f); AEROPUERTOS_MAP.put("PDX", 172.0f); AEROPUERTOS_MAP.put("PHL", 173.0f);
        AEROPUERTOS_MAP.put("PHX", 174.0f); AEROPUERTOS_MAP.put("PIT", 175.0f); AEROPUERTOS_MAP.put("PNS", 176.0f);
        AEROPUERTOS_MAP.put("PSC", 177.0f); AEROPUERTOS_MAP.put("PSE", 178.0f); AEROPUERTOS_MAP.put("PSG", 179.0f);
        AEROPUERTOS_MAP.put("PSP", 180.0f); AEROPUERTOS_MAP.put("PVD", 181.0f); AEROPUERTOS_MAP.put("PWM", 182.0f);
        AEROPUERTOS_MAP.put("RAP", 183.0f); AEROPUERTOS_MAP.put("RDM", 184.0f); AEROPUERTOS_MAP.put("RDU", 185.0f);
        AEROPUERTOS_MAP.put("RIC", 186.0f); AEROPUERTOS_MAP.put("RNO", 187.0f); AEROPUERTOS_MAP.put("ROA", 188.0f);
        AEROPUERTOS_MAP.put("ROC", 189.0f); AEROPUERTOS_MAP.put("RST", 190.0f); AEROPUERTOS_MAP.put("RSW", 191.0f);
        AEROPUERTOS_MAP.put("SAF", 192.0f); AEROPUERTOS_MAP.put("SAN", 193.0f); AEROPUERTOS_MAP.put("SAT", 194.0f);
        AEROPUERTOS_MAP.put("SAV", 195.0f); AEROPUERTOS_MAP.put("SBA", 196.0f); AEROPUERTOS_MAP.put("SBN", 197.0f);
        AEROPUERTOS_MAP.put("SBP", 198.0f); AEROPUERTOS_MAP.put("SCC", 199.0f); AEROPUERTOS_MAP.put("SCE", 200.0f);
        AEROPUERTOS_MAP.put("SDF", 201.0f); AEROPUERTOS_MAP.put("SEA", 202.0f); AEROPUERTOS_MAP.put("SFO", 203.0f);
        AEROPUERTOS_MAP.put("SGF", 204.0f); AEROPUERTOS_MAP.put("SHV", 205.0f); AEROPUERTOS_MAP.put("SIT", 206.0f);
        AEROPUERTOS_MAP.put("SJC", 207.0f); AEROPUERTOS_MAP.put("SJU", 208.0f); AEROPUERTOS_MAP.put("SLC", 209.0f);
        AEROPUERTOS_MAP.put("SMF", 210.0f); AEROPUERTOS_MAP.put("SNA", 211.0f); AEROPUERTOS_MAP.put("SRQ", 212.0f);
        AEROPUERTOS_MAP.put("STL", 213.0f); AEROPUERTOS_MAP.put("STS", 214.0f); AEROPUERTOS_MAP.put("STT", 215.0f);
        AEROPUERTOS_MAP.put("STX", 216.0f); AEROPUERTOS_MAP.put("SYR", 217.0f); AEROPUERTOS_MAP.put("TLH", 218.0f);
        AEROPUERTOS_MAP.put("TPA", 219.0f); AEROPUERTOS_MAP.put("TRI", 220.0f); AEROPUERTOS_MAP.put("TUL", 221.0f);
        AEROPUERTOS_MAP.put("TUS", 222.0f); AEROPUERTOS_MAP.put("TVC", 223.0f); AEROPUERTOS_MAP.put("TXK", 224.0f);
        AEROPUERTOS_MAP.put("TYS", 225.0f); AEROPUERTOS_MAP.put("VLD", 226.0f); AEROPUERTOS_MAP.put("VPS", 227.0f);
        AEROPUERTOS_MAP.put("WRG", 228.0f); AEROPUERTOS_MAP.put("XNA", 229.0f); AEROPUERTOS_MAP.put("YAK", 230.0f);
    }
    /**
     * Transforma un objeto FlightRequestDTO en un vector de entrada para el modelo
     * ONNX.
     *
     * @param request Datos del vuelo (origen, destino, fecha, aerolínea).
     * @return float[] Vector de 12 dimensiones preparado para la inferencia.
     *         Estructura del vector:
     *         [0] Mes
     *         [1] Día de la semana
     *         [2] Distancia
     *         [3] Seno (Ciclo Diario)
     *         [4] Coseno (Ciclo Diario)
     *         [5] ID Origen
     *         [6] ID Destino
     *         [7-11] One-Hot Encoding para Aerolínea
     */
    public float[] transformar(Flight request) {
        // 1. Validaciones de Datos
        // Protegemos al modelo de datos desconocidos que no sabría interpretar.
        if (!AEROPUERTOS_MAP.containsKey(request.getOrigen())) {
            throw new IllegalArgumentException("Origen desconocido: " + request.getOrigen());
        }
        if (!AEROPUERTOS_MAP.containsKey(request.getDestino())) {
            throw new IllegalArgumentException("Destino desconocido: " + request.getDestino());
        }

        // 2. Ingeniería de Características Temporales
        LocalDateTime fecha = request.getFechaPartida();
        float month = fecha.getMonthValue();
        float dayOfWeek = fecha.getDayOfWeek().getValue() - 1; // 0=Lunes

        // Transformación Cíclica del Tiempo:
        // Convertimos la hora en coordenadas (sin, cos) de un círculo de 24h.
        // Esto permite que el modelo entienda la continuidad entre 23:59 y 00:00.
        int horas = fecha.getHour();
        int minutos = fecha.getMinute();
        int minutosTotales = horas * 60 + minutos;
        double cicloDiario = 2 * Math.PI * minutosTotales / 1440.0; // 1440 minutos en un día

        /// 3. Construcción del Vector Base
        // El vector tiene tamaño 12: 7 características base + 5 para aerolíneas.
        float[] vector = new float[12];

        vector[0] = month;
        vector[1] = dayOfWeek;
        vector[2] = (float) request.getDistaciaKm();
        vector[3] = (float) Math.sin(cicloDiario);
        vector[4] = (float) Math.cos(cicloDiario);
        vector[5] = AEROPUERTOS_MAP.get(request.getOrigen()); // Label Encoding Origen
        vector[6] = AEROPUERTOS_MAP.get(request.getDestino()); // Label Encoding Destino

        // 4. One-Hot Encoding para Aerolínea
        // Las posiciones 7 a 11 representan las 5 aerolíneas conocidas.
        // Solo una de estas posiciones será 1.0, el resto 0.0.
        int offset = 7;
        String carrier = request.getAerolinea();

        int index = AEROLINEAS_CONOCIDAS.indexOf(carrier);
        if (index != -1) {
            // Encontramos la aerolínea, activamos su "bit" correspondiente
            // Ejemplo: Si es "AA" (índice 1), activamos vector[7+1] = vector[8]
            vector[offset + index] = 1.0f;
        } else {
            // Manejo de categorías desconocidas (Other)
            System.out.println("⚠️ Aerolínea no reconocida en entrenamiento: " + carrier);
            // Todas las posiciones de aerolínea quedan en 0, lo cual es un manejo válido
        }

        // DEBUG: Imprimimos el vector resultante para verificación manual
        System.out.print("Vector enviado a ONNX: [");
        for (float f : vector) System.out.print(f + ", ");
        System.out.println("]");

        return vector;
    }
}
