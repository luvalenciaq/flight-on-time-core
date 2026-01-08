package com.flightontime.core.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_predicciones")
@Data
public class PredictionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String aerolinea;
    private String origen;
    private String destino;
    @Column(name = "fecha_partida")
    private LocalDateTime fechaPartida;
    @Column(name = "distancia_km")
    private Double distanciaKm;
    @Column(name = "resultado_prediccion")
    private String resultadoPrediccion;
    private Double probabilidad;
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
