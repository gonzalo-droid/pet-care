# Changelog

Cambios de BestID por versión, para personas (no la lista de commits). Formato basado en
[Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/); versiones con [SemVer](https://semver.org/lang/es/).
El número de build es el mismo en Android e iOS (`app-versionCode` en `gradle/libs.versions.toml`).

## [0.6.0] - sin publicar (build 4)

Primera versión candidata al App Store y a Google Play (`release/0.6.0`).

### Agregado
- iPad y tablets: el contenido se centra con un ancho máximo y ya no se estira de borde a borde (D49).
- Ajustes: enlaces a la política de privacidad y a los términos, "Escríbenos" con la versión y el modelo del
  teléfono ya escritos, "Recomendar BestID" y "Calificar BestID".
- Android: canales de avisos separados para vacunas y para desparasitación y cuidados, que se pueden apagar por
  separado en los ajustes del sistema.
- Informes de errores con Firebase Crashlytics, solo en las versiones de tienda y sin datos de la mascota ni de
  la persona (D48).

### Cambiado
- La política de privacidad, la ficha de las tiendas y el texto de Privacidad en Ajustes cuentan qué envía el
  informe de errores.

### Para el equipo
- Una sola fuente de versión; `./gradlew check` falla si iOS y Android no coinciden.
- `TEAM_ID` pasa a `iosApp/Configuration/Secrets.xcconfig` (no se versiona) y hay `local.properties.example`.
- Tests de los ViewModels del formulario de vacuna, Inicio y Respaldo; los fakes viven en `:core:testing`.
- Flujo de ramas `release/X.Y.Z` con tags (D50).

## [0.5.0] - 2026-11-23 (TestFlight, build 3)

Beta para probar con familia y amigos.

### Agregado
- Mascotas con foto, carnet y datos del tutor; varias mascotas con selector.
- Cartilla de vacunas a mano mirando las fotos de la cartilla, ordenada por año con fechas en calendario.
- Inicio con lo próximo (vacuna o cuidado), cuenta regresiva y aviso cuando una fecha está por confirmar.
- Salud: desparasitaciones y otros cuidados con intervalo, y peso con gráfico.
- Avisos una semana antes, un día antes y el mismo día, sin internet, a la hora que elijas.
- Ficha en PDF para enviar a la veterinaria.
- Respaldo para pasar los datos a otro teléfono, Android o iPhone.
- Bienvenida en tres páginas.
