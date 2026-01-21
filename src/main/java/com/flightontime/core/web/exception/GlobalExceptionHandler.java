package com.flightontime.core.web.exception;

import com.flightontime.core.dto.ErrorResponse;
import com.flightontime.core.exception.FlightValidationException;
import com.flightontime.core.exception.ModelInferenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);
    // Manejo de errores de negocio (Validaciones propias)
    @ExceptionHandler({FlightValidationException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleValidationErrors(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                ex.getMessage(),
                null
        );
        return ResponseEntity.badRequest().body(error);
    }

    // Manejo de errores de Bean Validation (@Valid en el DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
                errors.put(e.getField(), e.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(new ErrorResponse(
                "INVALID_DATA",
                "Error en los datos de entrada",
                errors
        ));
    }

    // Manejo de errores del Modelo ONNX
    @ExceptionHandler(ModelInferenceException.class)
    public ResponseEntity<ErrorResponse> handleModelError(ModelInferenceException ex) {
        log.error("Error inferencia ONNX", ex);
        ErrorResponse error = new ErrorResponse(
                "MODEL_ERROR",
                "Error al procesar el modelo de predicción: " + ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Manejo Genérico
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Error inesperado", ex);
        ErrorResponse error = new ErrorResponse(
                "INTERNAL_ERROR",
                "Error interno del sistema",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

