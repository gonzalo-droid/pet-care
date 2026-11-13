# BestID

App móvil (Android e iOS) que digitaliza la cartilla de vacunación de mascotas con una foto, guarda el historial y avisa las próximas vacunas y desparasitaciones. Mercado inicial: Perú. Idioma de la UI: español (es-PE).

**Fuente de verdad:** `docs/SYSTEM_DESIGN.md`. Léelo completo antes de la primera tarea y vuelve a la sección que corresponda antes de cada hito. Si algo de este archivo choca con ese documento, gana el documento y avísame.

## Cómo trabajamos

- Trabaja por hitos (sección 17 del documento: M0 a M5), en orden. Al terminar cada hito, detente, resume lo hecho contra su criterio de terminado y espera mi revisión antes de seguir.
- Antes de escribir código de un hito, muéstrame un plan corto: archivos a crear, decisiones técnicas y riesgos.
- Lo marcado `[Pendiente]` en el documento no se decide en silencio. Usa el valor por defecto indicado y avísame, o pregúntame si no hay valor por defecto.
- Si una librería o API no se comporta como dice el documento (versiones, nombres de artefactos), verifica en la documentación oficial y cuéntame el cambio antes de seguir.
- Commits pequeños, en español, con prefijo: `feat:`, `fix:`, `chore:`, `test:`, `docs:`.

## Stack (sección 8)

- Kotlin Multiplatform + Compose Multiplatform (Material 3, Compose Resources). Última versión estable de cada librería al momento de crear el proyecto.
- Navigation Compose multiplataforma con rutas tipadas, ViewModel y Lifecycle multiplataforma, StateFlow con flujo unidireccional.
- Koin para inyección (no Hilt).
- Room KMP con el driver SQLite empaquetado. DataStore KMP para preferencias.
- kotlinx-datetime, kotlinx.serialization, Coil 3, Sentry Kotlin Multiplatform.
- Lectura de cartilla: detrás de la interfaz `CardReader`. **Por ahora sin Firebase** (D4): la tecnología de lectura se decide antes de M3.
- Ktor client (para la alternativa de servicio propio de la sección 10).
- Sin backend propio, sin login y sin sincronización en el MVP. Todo se guarda en el teléfono.

## Estructura

```
/androidApp          punto de entrada Android (AGP 9 no permite app + KMP en un mismo módulo)
/composeApp          raíz de la UI compartida: App(), navegación, Koin, MainViewController de iOS
/core/model          entidades y enums (Kotlin puro)
/core/designsystem   tokens, AppTheme, componentes con previews, íconos, fuente, catálogo
/core/ui             presentación compartida entre features: etiquetas (especie, sexo), fechas es-PE, plurales
/core/navigation     rutas tipadas; los features navegan solo a través de ellas
/core/domain         casos de uso, validaciones, próxima dosis, ReminderEngine y puertos (repositorios, avisos)
/core/data           Room, DataStore, repositorios (luego respaldo)
/core/platform       adaptadores Android e iOS de los puertos de plataforma (DatePicker, avisos; luego imágenes...)
/feature/*           home, cartilla, health, pet, onboarding: pantalla + ViewModel
/build-logic         plugins de convención de Gradle
/iosApp              proyecto Xcode
/docs                SYSTEM_DESIGN.md, DECISIONS.md, licencias y design/ (prototipo: capturas, HTML por pantalla y flujos)
```

Un feature nunca depende de otro feature. `core/designsystem` no depende de `core/model`. `domain` nunca depende de UI ni de código de plataforma. Ver `docs/DECISIONS.md` (D1).

## Reglas que no se rompen

1. **Design system primero:** colores, tipografía (Nunito incluida en la app), radios y espaciados salen solo de los tokens de la sección 7 vía `AppTheme`. Ninguna pantalla define colores o tamaños sueltos. Si falta una variante, se agrega al componente.
2. **Accesibilidad:** toques de 48 dp o más, ningún texto bajo 13 sp, contraste de 4,5:1 o más, estados nunca solo por color, `contentDescription` en íconos con acción, texto hasta 200 % sin cortes.
3. **Textos** en Compose Resources (es-PE), nunca escritos en el código.
4. **Código de plataforma** solo detrás de las interfaces de la sección 8 (`ImagePicker`, `ImageProcessor`, `NotificationScheduler`, `PdfRenderer`, `ShareSheet`, `FilePicker`, `CardReader`) y `DatePicker` (D14). La lógica de avisos vive en `ReminderEngine`, común y pura.
5. **La IA propone, la persona confirma:** nada leído de la cartilla se guarda sin confirmación, y un dato ilegible queda vacío, nunca se inventa.
6. **Privacidad:** sin datos personales en logs, Sentry ni analítica. Sin claves ni secretos en el repositorio. `google-services.json` y `GoogleService-Info.plist` van en `.gitignore`.
7. **Datos de prueba:** las fotos reales de cartillas no se suben al repositorio. Ponlas en `testdata/private/`, que va en `.gitignore`.
8. **Base de datos:** entidades con UUID, `createdAt`, `updatedAt` y `deletedAt`. Cada cambio de esquema trae su migración.
9. **Pruebas:** dominio, motor de avisos, validaciones y respaldo con pruebas en `commonTest`. Un hito no está terminado sin sus pruebas.

## Comandos

- Compilar Android: `./gradlew :androidApp:assembleDebug`
- Pruebas en la JVM (dominio, Room en memoria, host de Android): `./gradlew allTests` en Linux
- Pruebas comunes en el simulador iOS (solo en Mac): `./gradlew iosSimulatorArm64Test`
- iOS: abrir `iosApp/iosApp.xcodeproj` en Xcode, o `xcodebuild -project iosApp/iosApp.xcodeproj -target iosApp -sdk iphonesimulator ARCHS=arm64 CODE_SIGNING_ALLOWED=NO build`
- Catálogo de componentes en el teléfono: Ajustes (ícono arriba a la derecha) → "Ver catálogo de componentes"

## Fuera del MVP (no implementar)

Cuenta y nube, enlace compartible, tema oscuro (la app fuerza tema claro), citas, recetas, gastos, QR, panel para veterinarias, cobro.
