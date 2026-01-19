package com.flightontime.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "aeropuertos")
@Data
@NoArgsConstructor
public class Airport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String codigo;
    private String ciudad;

    private Double latitud;
    private Double longitud;

    public Airport(String codigo, String ciudad, Double latitud, Double longitud) {
        this.codigo = codigo;
        this.ciudad = ciudad;
        this.latitud = latitud;
        this.longitud = longitud;
    }
}
