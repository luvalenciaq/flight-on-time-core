# ✈️ Flight On Time Core

API REST para la predicción de puntualidad de vuelos utilizando modelos de Machine Learning (ONNX). Este microservicio es el núcleo de inferencia que procesa datos de vuelos y determina la probabilidad de retraso en tiempo real.

## 📋 Descripción

**Flight On Time Core** es una aplicación desarrollada en Java con Spring Boot que expone endpoints para consumir un modelo de inteligencia artificial pre-entrenado. Su función principal es recibir los detalles de un vuelo (aerolínea, origen, destino, fecha, etc.), transformarlos en vectores numéricos y ejecutar una inferencia utilizando **ONNX Runtime**.

## 🚀 Tecnologías utilizada

*   **Java 17**: Lenguaje principal del proyecto.
*   **Spring Boot**: Framework para la creación de la API REST.
*   **Microsoft ONNX Runtime**: Motor de alto rendimiento para ejecutar el modelo de ML.
*   **Maven**: Gestión de dependencias y construcción.
*   **Lombok**: Reducción de código repetitivo (getters, setters, constructores).

## 🛠️ Instalación y Configuración

### Prerrequisitos
*   JDK 17 instalado.
*   Maven instalado.

### Construcción
Para compilar el proyecto y descargar las dependencias, ejecuta:

```bash
./mvnw clean install
```

### Ejecución
Puedes iniciar la aplicación con el comando:

```bash
./mvnw spring-boot:run
```

La aplicación se iniciará generalmente en el puerto `8080` (o el configurado en `application.properties`).

## 🔌 API Endpoints

### 1. Predecir Puntualidad

Envía los datos de un vuelo para obtener una predicción.

*   **URL**: `/internal/predict`
*   **Método**: `POST`
*   **Content-Type**: `application/json`

#### Cuerpo de la Petición (JSON)

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `aerolinea` | String | Código o nombre de la aerolínea (ej. "AA", "Delta"). |
| `origen` | String | Código del aeropuerto de origen (ej. "JFK"). |
| `destino` | String | Código del aeropuerto de destino (ej. "LAX"). |
| `fecha_partida` | String | Fecha y hora de partida en formato ISO (ej. "2023-12-25T14:30:00"). |
| `distancia_km` | Number | Distancia del vuelo en kilómetros. |

**Ejemplo:**

```json
{
    "aerolinea": "Delta",
    "origen": "ATL",
    "destino": "JFK",
    "fecha_partida": "2023-11-20T08:00:00",
    "distancia_km": 1200.5
}
```

#### Respuesta Exitosa (200 OK)

```json
{
    "estado": "Puntual ✅",
    "probabilidad": 0.1234
}
```
*   `estado`: Clasificación textual ("Puntual ✅" o "Posible Retraso ⚠️").
*   `probabilidad`: Valor numérico (float) indicando la probabilidad de retraso (0.0 a 1.0).

#### Respuestas de Error
*   **400 Bad Request**: Si los datos de entrada son inválidos o faltan campos obligatorios.
*   **500 Internal Server Error**: Si hay un fallo al ejecutar el modelo ONNX.

## 📂 Estructura del Proyecto

*   `src/main/java/com/flightontime/core`:
    *   `CoreApplication.java`: Clase principal de arranque.
    *   `controller`: Controladores REST (`FlightController`).
    *   `service`: Lógica de negocio.
        *   `FlightPredictionService.java`: Carga y ejecuta el modelo ONNX.
        *   `FeatureEngineeringService.java`: Transforma los datos crudos en vectores para el modelo.
    *   `model`: Clases de dominio (`Flight`).
    *   `dto`: Objetos de transferencia de datos (`FlightRequestDTO`, `PredictionResponseDTO`).
*   `src/main/resources`:
    *   `modelo_vuelos.onnx`: Archivo del modelo de ML (debe estar presente aquí).

## 🧠 Lógica de Predicción

El proceso de inferencia sigue estos pasos:
1.  **Recepción**: El `FlightController` recibe el JSON y lo valida.
2.  **Transformación**: `FeatureEngineeringService` convierte las variables categóricas (aerolínea, aeropuertos) y numéricas en un vector `float[]` compatible con el modelo.
3.  **Inferencia**: `FlightPredictionService` utiliza la sesión de ONNX Runtime para procesar el vector.
4.  **Interpretación**: Se analiza la probabilidad de salida; si es mayor a 0.5 (50%), se clasifica como retraso.
