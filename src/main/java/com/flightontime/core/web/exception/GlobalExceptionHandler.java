package com.flightontime.core.web.exception;

import com.flightontime.core.exception.FlightValidationException;
import com.flightontime.core.exception.ModelInferenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FlightValidationException.class)
    public ResponseEntity<Map<String, Object>> handleFlightValidation(
            FlightValidationException ex
    ) {
        log.warn("Error de validación de vuelo: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadInput(
            IllegalArgumentException ex
    ) {
        return ResponseEntity.badRequest().body(buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        ));
    }

    @ExceptionHandler(ModelInferenceException.class)
    public ResponseEntity<Map<String, Object>> handleModelError(
            ModelInferenceException ex
    ) {
        log.error("Error durante la inferencia del modelo ONNX", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex
    ) {
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error interno del sistema"
                ));
    }
    private Map<String, Object> buildResponse(
            HttpStatus status,
            String message
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return response;
    }
}

