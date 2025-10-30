FEM P2 — Aplicación de planificación de viajes

FEM P2 es una aplicación Android basada en Jetpack Compose, diseñada para ayudar a planificar viajes a Madrid. La app integra Firebase Authentication y almacenamiento en la nube, permitiendo crear y gestionar itinerarios, además de mostrar el clima en tiempo real, historial meteorológico y titulares de noticias oficiales de la ciudad de Madrid.

Funcionalidades principales

Autenticación de usuarios
Registro, inicio y cierre de sesión con correo electrónico mediante Firebase Authentication.

Gestión de itinerarios de viaje
Guarda planes de viaje en Firebase Cloud Firestore, permite visualizar y añadir itinerarios.

Panel del clima en Madrid
Obtiene el clima actual desde la API de Open-Meteo y almacena hasta 10 registros históricos.

Historial meteorológico emergente
Puedes consultar en cualquier momento las últimas consultas del clima a través de un diálogo dedicado.

Noticias de Madrid
Obtención de titulares recientes desde el portal oficial madrid.es utilizando Jsoup.
(Funcionalidad parcialmente implementada — los titulares se refrescan al actualizar el clima).
app/
├── src/main/java/com/example/fem_p2
│   ├── data/                # Capa de repositorios (auth, clima, noticias, itinerarios)
│   ├── ui/                  # Interfaz basada en Jetpack Compose
│   │   ├── auth/            # Pantallas y estados de login/registro
│   │   ├── home/            # Pantalla principal, historial climático, noticias
│   │   └── navigation/      # Gráfico de navegación de Compose
│   ├── TravelPlannerApp.kt  # Clase Application y configuración
│   └── TravelPlannerContainer.kt # Instancias de Firebase, Retrofit, etc.
└── build.gradle.kts         # Script de compilación de nivel superior
La app sigue la arquitectura MVVM:
Los ViewModel interactúan con Firebase y APIs externas a través de repositorios, exponiendo StateFlow para que las pantallas Compose reaccionen a los cambios. Las corrutinas se usan para operaciones asíncronas y control de estado.

Requisitos del entorno

Android Studio Koala o posterior

Android SDK 24 o superior (objetivo SDK 36)

JDK 17

Proyecto Firebase configurado
(Authentication y Cloud Firestore habilitados)