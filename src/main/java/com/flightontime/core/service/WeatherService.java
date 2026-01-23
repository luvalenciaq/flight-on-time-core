package com.flightontime.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.flightontime.core.dto.WeatherFeaturesDTO;
import com.flightontime.core.model.Airport;
import com.flightontime.core.repository.AirportRepository;
import com.flightontime.core.web.exception.GlobalExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherService {
    private final RestTemplate restTemplate;
    private final AirportRepository airportRepository;
    private static final Logger log =
            LoggerFactory.getLogger(WeatherService.class);

    private final String USER_AGENT = "(FlightOnTimeApp, contacto@tuempresa.com)";

    public WeatherFeaturesDTO obtenerFeaturesClimaticas(
            String codigoAeropuerto,
            LocalDateTime fechaVuelo
    ) {
        log.info("Buscando aeropuerto con código: {}", codigoAeropuerto);

        // 1. Obtener coordenadas
        Airport airport = airportRepository.findAll().stream()
                .filter(a -> a.getCodigo().equals(codigoAeropuerto))
                .findFirst()
                .orElse(null);

        if (airport == null || airport.getLatitud() == null) {
            System.err.println("Aeropuerto sin coordenadas: " + codigoAeropuerto);
            return new WeatherFeaturesDTO(0.0, 0.0, 0, 0, 0); // Valores default neutros
        }
        log.info("Aeropuerto encontrado: {}", airport.getNombre());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 2. Obtener URL del Grid (Paso 1 NWS API)
            String pointsUrl = String.format("https://api.weather.gov/points/%s,%s", airport.getLatitud(), airport.getLongitud());
            ResponseEntity<JsonNode> pointsResponse = restTemplate.exchange(pointsUrl, HttpMethod.GET, entity, JsonNode.class);

            if (pointsResponse.getBody() == null) return new WeatherFeaturesDTO(0.0, 0.0, 0, 0, 0);

            // 3. Obtener URL del pronóstico HORARIO (forecastHourly)
            // Usamos hourly porque necesitamos calcular rangos (Max - Min)
            String forecastHourlyUrl = pointsResponse.getBody().path("properties").path("forecastHourly").asText();

            // 4. Obtener Pronóstico
            ResponseEntity<JsonNode> forecastResponse = restTemplate.exchange(forecastHourlyUrl, HttpMethod.GET, entity, JsonNode.class);
            JsonNode periods = forecastResponse.getBody().path("properties").path("periods");

            // 5. Calcular Variables
            return calcularVariablesDiarias(periods, fechaVuelo.toLocalDate());

        } catch (Exception e) {
            System.err.println("Error NWS API: " + e.getMessage());
            return new WeatherFeaturesDTO(20.0, 10.0, 0, 0, 0); // Fallback razonable
        }
    }

    private WeatherFeaturesDTO calcularVariablesDiarias(JsonNode periods, LocalDate diaVuelo) {
        List<Double> temps = new ArrayList<>();
        List<Double> dews = new ArrayList<>();
        boolean precip = false;
        boolean snow = false;
        boolean wind = false;

        for (JsonNode period : periods) {
            // Parsear fecha del periodo
            String startTimeStr = period.path("startTime").asText();
            // Formato ISO: 2026-01-15T14:00:00-05:00. Tomamos la parte de la fecha.
            OffsetDateTime odt = OffsetDateTime.parse(startTimeStr);
            LocalDate periodDate = odt.toLocalDate();

            // Solo nos importan los periodos del día del vuelo
            if (periodDate.equals(diaVuelo)) {

                // Temperatura (Generalmente viene en F)
                double tempF = period.path("temperature").asDouble();
                temps.add(tempF);

                // Dewpoint (Viene en objeto value/unitCode, suele ser C, convertir a F)
                // Formula C a F: (C * 9/5) + 32
                JsonNode dewNode = period.path("dewpoint").path("value");
                if (!dewNode.isNull()) {
                    double dewC = dewNode.asDouble();
                    double dewF = (dewC * 9 / 5) + 32;
                    dews.add(dewF);
                }

                // Lluvia/Nieve (Miramos shortForecast o probability)
                String shortForecast = period.path("shortForecast").asText().toLowerCase();
                if (shortForecast.contains("rain") || shortForecast.contains("shower")) precip = true;
                if (shortForecast.contains("snow") || shortForecast.contains("blizzard")) snow = true;

                // Viento (Viene como "10 mph", hay que limpiar y convertir a Nudos si es necesario)
                // El modelo pide High Wind si >= 15 kts. 15 kts ~= 17 mph.
                String windStr = period.path("windSpeed").asText().split(" ")[0]; // "15 mph" -> "15"
                try {
                    double windMph = Double.parseDouble(windStr);
                    if (windMph >= 17) wind = true;
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (temps.isEmpty()) return new WeatherFeaturesDTO(0.0, 0.0, 0, 0, 0);

        // Cálculos finales
        double maxTemp = temps.stream().max(Double::compare).orElse(0.0);
        double minTemp = temps.stream().min(Double::compare).orElse(0.0);
        double tempRange = maxTemp - minTemp;

        double maxDew = dews.stream().max(Double::compare).orElse(0.0);
        double minDew = dews.stream().min(Double::compare).orElse(0.0);
        double dewRange = maxDew - minDew;

        log.info("📊 Features clima: temp_range={}, dewpoint={}, precip={}, snow={}, wind={}",
                tempRange, dewRange, precip, snow, wind);


        return new WeatherFeaturesDTO(
                tempRange,
                dewRange,
                precip ? 1 : 0,
                snow ? 1 : 0,
                wind ? 1 : 0
        );
    }
}
