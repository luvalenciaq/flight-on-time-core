package com.flightontime.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "aerolineas")
@Data
@NoArgsConstructor
public class Airline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;
    private String nombre;

    public Airline(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }
}
