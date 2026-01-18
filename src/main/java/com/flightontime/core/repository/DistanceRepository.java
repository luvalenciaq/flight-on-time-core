package com.flightontime.core.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightontime.core.model.Distance;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DistanceRepository extends JpaRepository<Distance, Long> {

    Optional<Distance> findByOrigen_CodigoAndDestino_Codigo(
            String origenCodigo,
            String destinotionCodigo
    );

    @Query("SELECT d.distanciaKm FROM Distance d " +
            "WHERE d.origen.codigo = :origenCodigo " +
            "AND d.destino.codigo = :destinoCodigo")
    Optional<Double> findDistanciaKmByRoute(
            @Param("origenCodigo") String origenCodigo,
            @Param("destinoCodigo") String destinoCodigo
    );

    boolean existsByOrigen_CodigoAndDestino_Codigo(
            String origenCodigo,
            String destinoCodigo
    );
}
