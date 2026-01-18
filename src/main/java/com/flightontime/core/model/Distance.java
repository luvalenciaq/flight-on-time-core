package com.flightontime.core.model;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "distancias")
public class Distance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "origen_aeropuerto_id", nullable = false)
    private Airport origen;

    @ManyToOne
    @JoinColumn(name = "destino_aeropuerto_id", nullable = false)
    private Airport destino;

    @Column(name = "distancia_km", nullable = false)
    private Double distanciaKm;
}
