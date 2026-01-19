package com.flightontime.core.service;

import com.flightontime.core.dto.WeatherDataDTO;
import com.flightontime.core.model.Airport;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private final RestTemplate restTemplate;

    public WeatherService() {
        this.restTemplate = new RestTemplate();
        // Configurar User-Agent requerido por Weather.gov
        this.restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("User-Agent", "FlightOnTime/1.0 (contact@flightontime.com)");
            return execution.execute(request, body);
        });
    }

    /**
     * Obtiene datos meteorológicos para un aeropuerto en una fecha específica
     *
     * @param airport Aeropuerto con coordenadas
     * @param dateTime Fecha y hora del vuelo
     * @return DTO con datos del clima o valores por defecto si hay error
     */
    public WeatherDataDTO getWeatherData(Airport airport, LocalDateTime dateTime) {
        log.info("🌤️ Obteniendo clima para aeropuerto: {}", airport.getCodigo());
        try {
            // Validar coordenadas
            if (airport.getLatitud() == null || airport.getLongitud() == null) {
                log.warn("Aeropuerto {} no tiene coordenadas, usando valores por defecto", airport.getCodigo());
                return getDefaultWeather();
            }

            double lat = airport.getLatitud().doubleValue();
            double lon = airport.getLongitud().doubleValue();
            log.info("📍 Coordenadas: lat={}, lon={}", lat, lon);

            // 1. Obtener el punto de grid más cercano
            String pointsUrl = String.format("https://api.weather.gov/points/%.4f,%.4f", lat, lon);
            log.info("🌐 Llamando a Weather.gov: {}", pointsUrl);

            @SuppressWarnings("unchecked")
            Map<String, Object> pointsResponse = restTemplate.getForObject(pointsUrl, Map.class);

            if (pointsResponse == null || !pointsResponse.containsKey("properties")) {
                return getDefaultWeather();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) pointsResponse.get("properties");
            String forecastUrl = (String) properties.get("forecast");
            log.info("🌐 URL del pronóstico: {}", forecastUrl);

            // 2. Obtener pronóstico
            @SuppressWarnings("unchecked")
            Map<String, Object> forecastResponse = restTemplate.getForObject(forecastUrl, Map.class);

            if (forecastResponse == null || !forecastResponse.containsKey("properties")) {
                return getDefaultWeather();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> forecastProperties = (Map<String, Object>) forecastResponse.get("properties");

            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> periods =
                    (java.util.List<Map<String, Object>>) forecastProperties.get("periods");

            if (periods == null || periods.isEmpty()) {
                log.warn("⚠️ No hay períodos de pronóstico disponibles");
                return getDefaultWeather();
            }

            // Analizar múltiples períodos para calcular rangos reales
            return analyzeMultiplePeriods(periods);

        } catch (Exception e) {
            log.error("Error obteniendo datos del clima para {}: {}", airport.getCodigo(), e.getMessage());
            return getDefaultWeather();
        }
    }

    /**
     * Analiza múltiples períodos para calcular rangos reales según la documentación
     * temp_range_f = max_temp_f - min_temp_f
     */
    private WeatherDataDTO analyzeMultiplePeriods(java.util.List<Map<String, Object>> periods) {
        // Analizar los primeros 5 períodos (cubre ~24 horas)
        int numPeriods = Math.min(5, periods.size());

        int maxTemp = Integer.MIN_VALUE;
        int minTemp = Integer.MAX_VALUE;
        int maxWind = 0;
        boolean hasSnowCondition = false;
        boolean hasPrecipCondition = false;

        for (int i = 0; i < numPeriods; i++) {
            Map<String, Object> period = periods.get(i);

            // Temperatura
            Integer temp = (Integer) period.get("temperature");
            if (temp != null) {
                maxTemp = Math.max(maxTemp, temp);
                minTemp = Math.min(minTemp, temp);
            }

            // Viento
            Integer windSpeed = parseWindSpeed((String) period.get("windSpeed"));
            if (windSpeed != null) {
                maxWind = Math.max(maxWind, windSpeed);
            }

            // Pronóstico
            String forecast = (String) period.get("shortForecast");
            if (forecast != null) {
                String lowerForecast = forecast.toLowerCase();
                if (lowerForecast.contains("rain") || lowerForecast.contains("shower")) {
                    hasPrecipCondition = true;
                }
                if (lowerForecast.contains("snow")) {
                    hasSnowCondition = true;
                }
            }
        }

        //Calcular temp_range según documentación: max_temp - min_temp
        float tempRangeF = (maxTemp > minTemp) ? (float)(maxTemp - minTemp) : 15.0f;

        // Dewpoint proporcional al rango de temperatura (aproximación estándar)
        float dewpointRangeF = tempRangeF * 0.7f;

        // Features binarios
        float hasPrecip = hasPrecipCondition ? 1.0f : 0.0f;
        float hasSnow = hasSnowCondition ? 1.0f : 0.0f;
        float highWind = maxWind >= 17 ? 1.0f : 0.0f;

        log.info("✅ Clima analizado de {} períodos: minTemp={}°F, maxTemp={}°F, maxWind={}mph",
                numPeriods, minTemp, maxTemp, maxWind);
        log.info("📊 Features clima: temp_range={}, dewpoint={}, precip={}, snow={}, wind={}",
                tempRangeF, dewpointRangeF, hasPrecip, hasSnow, highWind);

        return new WeatherDataDTO(tempRangeF, dewpointRangeF, hasPrecip, hasSnow, highWind);
    }

    /**
     * Extrae velocidad del viento en mph del string "10 mph" o "10 to 15 mph"
     */
    private Integer parseWindSpeed(String windSpeedStr) {
        if (windSpeedStr == null) return 5;
        try {
            // Extraer primer número del string
            String[] parts = windSpeedStr.split(" ");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return 5;
        }
    }

    /**
     * Valores por defecto cuando no hay datos disponibles
     */
    private WeatherDataDTO getDefaultWeather() {
        log.info("🔧 Usando valores por defecto del clima");
        return new WeatherDataDTO(15.0f, 10.0f, 0.0f, 0.0f, 0.0f);
    }
}