package com.flightontime.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Flight {
    private String aerolinea;
    private String origen;
    private String destino;
    private LocalDateTime fechaPartida;
    private double distaciaKm;

    public double getDistaciaKm() {
        return distaciaKm;
    }
}
