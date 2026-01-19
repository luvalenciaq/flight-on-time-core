package com.flightontime.core;

import ai.onnxruntime.OrtEnvironment;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@OpenAPIDefinition(
		info = @io.swagger.v3.oas.annotations.info.Info(
				title = "Flight On Time Core",
				version = "1.0",
				description = "API para predicciones de vuelos"
		)
)

@SpringBootApplication
@EnableAsync
public class CoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreApplication.class, args);
	}

	@Bean
	public OrtEnvironment ortEnvironment() {
		// Inicializa el entorno de ONNX Runtime
		return OrtEnvironment.getEnvironment();
	}
}