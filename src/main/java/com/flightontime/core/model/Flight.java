package com.flightontime.core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "vuelos")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_aerolinea", nullable = false)
    private Airline aerolinea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_aeropuerto_origen", nullable = false)
    private Airport origen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_aeropuerto_destino", nullable = false)
    private Airport destino;

    @Column(name = "fecha_partida", nullable = false)
    private LocalDateTime fechaPartida;

    @Column(name = "fecha_creacion", updatable = false, nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PredictionResult> predicciones = new ArrayList<>();
}
