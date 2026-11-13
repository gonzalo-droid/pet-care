# BestID: cartilla digital de tu mascota

App móvil para Android e iOS que digitaliza la cartilla de vacunación de perros y gatos, guarda su historial y avisa cuándo toca la próxima vacuna o desparasitación. Mercado inicial: Perú (UI en español es-PE). Todos los datos se guardan en el teléfono: no hay cuenta ni servidor.

La especificación completa está en [`docs/SYSTEM_DESIGN.md`](docs/SYSTEM_DESIGN.md) y las decisiones tomadas durante el desarrollo, en [`docs/DECISIONS.md`](docs/DECISIONS.md).

## Estado

| Hito | Contenido | Estado |
|---|---|---|
| M0 | Proyecto multimódulo, design system con previews, 4 pestañas, CI Android e iOS | Hecho |
| M1 | Mascotas (crear, editar, eliminar, varias, selector), vacunas a mano, Cartilla, Detalle, Inicio | Hecho |
| M2 | Motor de avisos, notificaciones, Salud (cuidados y peso con gráfico) | Hecho en código; falta la prueba en teléfonos reales |
| M3 | Lectura de la cartilla con foto | Pendiente: falta elegir la tecnología de lectura |
| M4 | PDF para compartir, respaldo, Ajustes | Pendiente |
| M5 | Estados vacíos y de error, accesibilidad, textos finales, beta cerrada | Pendiente |

## Pantallas

Capturas del prototipo de diseño ([`docs/design/`](docs/design/)), que la app sigue. Las capturas de la app real se generan en el CI: artefacto `ios-capturas` de cada ejecución, con los flujos de M1 y M2 en el simulador de iOS.

| Inicio | Cartilla | Detalle de vacuna | Nueva vacuna |
|---|---|---|---|
| <img src="docs/design/screens/06-inicio.png" width="200" alt="Inicio"> | <img src="docs/design/screens/07-cartilla.png" width="200" alt="Cartilla"> | <img src="docs/design/screens/08-detalle-vacuna.png" width="200" alt="Detalle de vacuna"> | <img src="docs/design/screens/12-nueva-vacuna.png" width="200" alt="Nueva vacuna"> |

| Mascota (carnet) | Nueva mascota | Salud: cuidados | Salud: peso |
|---|---|---|---|
| <img src="docs/design/screens/11-mascota-carnet.png" width="200" alt="Carnet de la mascota"> | <img src="docs/design/screens/13-nueva-mascota.png" width="200" alt="Nueva mascota"> | <img src="docs/design/screens/09-salud-cuidados.png" width="200" alt="Salud: cuidados"> | <img src="docs/design/screens/10-salud-peso.png" width="200" alt="Salud: peso"> |

| Bienvenida | Fotos de la cartilla | Revisar lectura | Compartir ficha |
|---|---|---|---|
| <img src="docs/design/screens/01-bienvenida.png" width="200" alt="Bienvenida"> | <img src="docs/design/screens/02-fotos-cartilla.png" width="200" alt="Fotos de la cartilla"> | <img src="docs/design/screens/04-revisar-lectura.png" width="200" alt="Revisar lectura"> | <img src="docs/design/screens/14-compartir-ficha.png" width="200" alt="Compartir ficha"> |

Componentes y tokens: [`docs/design/screens/15-componentes.png`](docs/design/screens/15-componentes.png).

## Stack

- Kotlin Multiplatform con Compose Multiplatform (Material 3, Compose Resources)
- Navigation Compose con rutas tipadas, ViewModel y StateFlow con flujo unidireccional
- Koin para inyección
- Room KMP con SQLite empaquetado y DataStore, todo local
- kotlinx-datetime, kotlinx.serialization y Coil 3

Las versiones están en [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Estructura del proyecto

```
androidApp/          Punto de entrada Android (Application, MainActivity, manifiesto, ícono)
composeApp/          Raíz de la UI compartida: App(), navegación, Koin, MainViewController de iOS
core/
  model/             Entidades y enums (Kotlin puro)
  domain/            Casos de uso, validaciones, próxima dosis, motor de avisos, puertos
  data/              Room, DataStore y repositorios; esquemas en core/data/schemas/
  designsystem/      Tokens, AppTheme, componentes con previews, íconos, fuente Nunito, catálogo
  ui/                Presentación compartida entre features: especie, sexo, fechas es-PE, plurales
  navigation/        Rutas tipadas; los features navegan solo a través de ellas
  platform/          Adaptadores Android e iOS: selector de fecha y avisos locales
feature/
  home/              Inicio
  cartilla/          Cartilla, detalle y formulario de vacuna
  health/            Salud (cuidados y peso)
  pet/               Carnet y formulario de mascota
  onboarding/        Primer uso (por ahora, el permiso de avisos)
build-logic/         Plugins de convención de Gradle (bestid.kmp.library, .compose, .feature)
iosApp/              Proyecto Xcode
docs/                SYSTEM_DESIGN.md, DECISIONS.md, licencias y design/ (prototipo)
.github/             CI: compila Android e iOS, corre las pruebas y abre la app en el simulador
```

Reglas de dependencia:
- Un feature nunca depende de otro feature.
- `core/designsystem` no depende de `core/model`.
- `core/domain` no depende de UI ni de código de plataforma.

## Cómo correrla

Requisitos: JDK 21, Android Studio (aporta el Android SDK, que Gradle necesita incluso para compilar iOS) y, para iOS, un Mac con Xcode.

- **Android:** abre el proyecto en Android Studio y corre `androidApp`, o por terminal `./gradlew :androidApp:assembleDebug`.
- **iOS:** abre `iosApp/iosApp.xcodeproj` en Xcode y corre en un simulador. La primera compilación descarga Kotlin/Native y tarda varios minutos. Para un iPhone real, completa `TEAM_ID` en `iosApp/Configuration/Config.xcconfig`.
- **Pruebas:** `./gradlew allTests` (JVM y host de Android). En Mac, también `./gradlew iosSimulatorArm64Test`, que incluye los flujos de M1 y M2 en el simulador.
- **Avisos:** Ajustes → "Probar aviso en 1 minuto" programa un aviso de prueba; cierra la app (o reinicia el teléfono) y espera.
- **Catálogo de componentes en el teléfono:** Ajustes (engranaje arriba a la derecha) → "Ver catálogo de componentes".

## Licencias de terceros

Fuente Nunito (SIL OFL) e íconos Material Symbols (Apache 2.0): ver [`docs/licenses/`](docs/licenses/).
