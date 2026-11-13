# Decisiones

Registro de decisiones tomadas durante el desarrollo que cambian o precisan `SYSTEM_DESIGN.md`. La más reciente va arriba. Cada una dice quién la tomó y en qué hito.

## 2026-11-13 · M1

### D17. Módulo `core/ui` `[Propuesta]`
Los textos y formatos que comparten varios features (especie con concordancia "Gata", sexo, fechas "21 dic 2025" y dd/MM/aaaa, "1 día / 2 días", "N vacunas registradas", avatar por especie) viven en `core/ui`. Un feature no depende de otro y el design system no conoce el modelo, así que este es el único lugar común. Depende de `core/designsystem` y `core/model`.

### D18. Fotos fuera de M1 `[Decidido por Gonzalo]`
Foto de la mascota y foto del sticker se hacen junto con `ImagePicker` e `ImageProcessor` (cuando haya captura). Mientras tanto, el avatar provisional por especie. Peso y Salud quedan para M2; Inicio muestra el peso como "Sin registro".

### D19. Formulario de vacuna sin hoja C4 `[Propuesta]`
Sin escaneo (D4), "Agregar vacuna" abre directo el formulario a mano. La hoja "Escanear o a mano" (C4) vuelve cuando exista la lectura. Cada frasco del formulario tiene producto, lote, elaboración y vencimiento (el prototipo dibuja solo producto y lote; la sección 9 pide también las fechas para validar el vencimiento).

### D20. Pruebas de datos en la JVM `[Propuesta]`
`core/model`, `core/domain` y `core/data` agregan un target `jvm()` solo para pruebas: Room en memoria con el driver empaquetado corre en la JVM sin Android. `composeApp` tiene una prueba que verifica el grafo de Koin.

### D21. Catálogo de vacunas en el dominio `[Propuesta]`
Los nombres del catálogo semilla ("Calicivirus", "Tos de perreras"...) son datos de dominio, no textos de interfaz: se guardan en la base, van al PDF y al respaldo, y los valida el veterinario. Viven en `VaccineCatalog` (`core/domain`) y se insertan en la base la primera vez que se leen.


## 2026-11-13 · Kickoff y M0

### D16. Componentes alineados con el prototipo (`docs/design/`) `[Propuesta]`
Gonzalo pasó el prototipo: capturas de las 15 pantallas, el HTML de cada una y el HTML de los flujos, en `docs/design/`. Los componentes se ajustaron a él:
- Bordes de 2 dp; campos de 54 dp con etiqueta 16/800 y texto 17/700; botón principal con texto 18/800 y secundarios con 16/800.
- ChipToggle y ChipChoice son filas con casilla o círculo visible (alto 52, radio 18), no píldoras. Los Badge son píldoras 14/800 sin ícono.
- InfoCard con radio 28; ReviewField con ícono y "Revisa: …" en una línea; selector de mascota con avatar de 40 y nombre 18/800; botón de ícono circular con borde.
- Íconos de pestaña de contorno en ambos estados: casa, portapapeles con check, corazón y huella. Ajustes usa deslizadores.
- Avatar sin foto: el gato provisional del prototipo para gatos y la huella para el resto.

Diferencias que se mantienen a propósito:
- Botón principal de 58 dp (el documento) y no de 56 (el prototipo): gana el documento.
- Bordes de campos, chips y botones secundarios en `outlineStrong`, no en `line` ni `outline` (D2: contraste de 3:1).
- Texto no seleccionado del control segmentado en `ink` (D2).
- Display de 32 (el documento) y no de 30 (el prototipo).

### D1. Estructura multimódulo con `androidApp` separado `[Decidido por Gonzalo]`
El Android Gradle Plugin 9 ya no permite aplicar `com.android.application` y Kotlin Multiplatform en el mismo módulo. Se sigue la plantilla oficial de JetBrains y se separa el punto de entrada de Android. Gonzalo pidió además clean code, arquitectura multimódulo y adaptadores por plataforma.

```
/androidApp          punto de entrada Android (Application, MainActivity, manifiesto, ícono)
/composeApp          raíz de la UI compartida: App(), navegación entre pestañas, arranque de Koin,
                     MainViewController de iOS. Genera el framework "ComposeApp" para Xcode
/core/model          entidades y enums (sección 9). Kotlin puro + kotlinx-datetime
/core/designsystem   tokens, AppTheme, componentes con previews, íconos, fuente Nunito, catálogo
/core/navigation     rutas tipadas (@Serializable). Los features navegan solo a través de ellas
/feature/home        Inicio
/feature/cartilla    Cartilla
/feature/health      Salud
/feature/pet         Mascota
/build-logic         plugins de convención de Gradle (bestid.kmp.library, .compose, .feature)
/iosApp              proyecto Xcode
```

Reglas de dependencia:
- `feature/*` depende de `core/*`, nunca de otro feature.
- `core/designsystem` no depende de `core/model`: los componentes reciben textos y tipos de UI (por ejemplo `AvatarSpecies`), y cada feature traduce su modelo.
- `core/domain` (casos de uso, `ReminderEngine`, validaciones, **puertos** de plataforma), `core/data` (Room, repositorios, respaldo) y `core/platform` (**adaptadores** Android e iOS de los puertos) se crean en el hito que los necesita por primera vez (M1 y M2), no vacíos en M0.

Puertos y adaptadores: las interfaces de la sección 8 (`ImagePicker`, `NotificationScheduler`, `CardReader`, etc.) se declaran en `core/domain` como puertos. Sus implementaciones viven en `core/platform` (`androidMain` / `iosMain`) y se registran en Koin. `CardReader` en iOS se implementa en Swift (SDK de Firebase) y se inyecta desde `iOSApp.swift`.

### D2. Contraste: token `outlineStrong` y texto `ink` en el control segmentado `[Decidido por Gonzalo]`
- `outline` (#CDBF9F) sobre `surface` da 1,82:1. WCAG 1.4.11 pide 3:1 para el borde que delimita un control. Se agrega **`outlineStrong` = #A48C55** (3,25:1 sobre surface, 3,02:1 sobre bg) para los bordes de botón secundario, botón punteado, campos y chips. `outline` queda para separadores decorativos.
- `muted` sobre `track` daba 4,54:1. El texto no seleccionado del control segmentado usa `ink` (9,61:1); el seleccionado se distingue por fondo blanco y peso 800.
- Los pares usados se verifican en `ContrastTest` (commonTest del design system).

### D3. Ktor incluido `[Decidido por Gonzalo]`
Ktor queda en el catálogo de versiones (OkHttp en Android, Darwin en iOS) para la alternativa de servicio propio de la sección 10. En M0 no lo usa ningún módulo.

### D4. Solo datos locales y sin Firebase por ahora `[Decidido por Gonzalo]`
Los datos viven solo en el teléfono y la app no usa Firebase por ahora: ni Firebase AI Logic ni App Check. Antes de M3 hay que decidir cómo se lee la cartilla (o si el escaneo se pospone); hasta entonces la app funciona con registro a mano. `CardReader` sigue siendo la interfaz que aísla esa decisión.

### D5. Analítica: no por ahora `[Decidido por Gonzalo]`
La sección 16 queda en pausa. No se agrega PostHog ni otra herramienta.

### D6. Intervalo de cuidados: `intervalValue` + `intervalUnit` `[Decidido por Gonzalo]`
`CareTask.intervalDays` se reemplaza por `intervalValue: Int?` + `intervalUnit: IntervalUnit? (DAY, MONTH)`. Los meses se suman como meses de calendario (en M2, dentro de `ReminderEngine`).

### D7. Tipos escritos a mano desde la lectura `[Decidido por Gonzalo, se detalla en M3]`
El modelo devuelve `{"id": "custom", "label": "..."}`. Al confirmar en O4 se busca un `VaccineType` con la misma etiqueta normalizada; si no existe, se crea `custom:<uuid>`.

### D8. `ScanStatus` básico `[Propuesta, se revisa en M3]`
`PENDING`, `READING`, `DONE`, `FAILED`, `CANCELLED`.

### D9. Íconos: Material Symbols Rounded como vectores `[Decidido por Gonzalo]`
`material-icons-extended` de Compose Multiplatform está congelado en 1.7.3 y no trae Material Symbols. Se importan solo los SVG usados (paquete npm `@material-symbols/svg-400`, estilo rounded, peso 400) convertidos a vector XML en `core/designsystem/composeResources/drawable`. Licencia Apache 2.0 en `docs/licenses/`.

### D10. Fuente Nunito estática `[Propuesta]`
Google Fonts publica Nunito solo como fuente variable. Se generaron instancias estáticas 500, 700 y 800 con `fonttools varLib.instancer` para que el peso se vea igual en Android e iOS. Licencia OFL en `docs/licenses/`.

### D11. ID de la app `com.bestid.app` `[Decidido por Gonzalo]`
Android `applicationId` y `BUNDLE_ID` de iOS. Reemplaza el valor por defecto `pe.<dominio>.bestid` de la sección 20.

### D12. Sentry fuera de M0 `[Decidido por Gonzalo]`
Se agrega cuando Gonzalo tenga el DSN. Se leerá de `local.properties` o de un secreto de CI y quedará apagado si falta.

### D13. Versiones `[Propuesta]`
Últimas estables al 13/11/2026: Kotlin 2.4.21, Compose Multiplatform 1.12.2, Material 3 1.9.0 (la que fija el plugin de Compose 1.12.2), AGP 9.5.1, Gradle 9.9.1, Navigation 2.10.0, Lifecycle 2.11.0, Koin 4.2.2, Coroutines 1.11.0, Serialization 1.13.0, kotlinx-datetime 0.8.0, Ktor 3.6.1, Coil 3.6.3, Turbine 1.2.1, Room 2.8.5 (`androidx.room`, no `room3`), DataStore 1.3.0. `compileSdk` y `targetSdk` 37, `minSdk` 26 (Android 8.0) e iOS 15.3 como mínimo `[Decidido por Gonzalo]`.

### D14. Selector de fecha nativo detrás de un puerto `[Decidido por Gonzalo]`
La sección 7 pide el selector **nativo** de fecha. Se agrega a las interfaces de plataforma: `DatePicker` con adaptadores `DatePickerDialog` (Android) y `UIDatePicker` (iOS), en M1. `DateField` sigue siendo de solo lectura con `onClick`.

### D15. Datos de ejemplo en previews y catálogo `[Propuesta]`
Las previews y el catálogo de componentes (herramienta de desarrollo, se abre desde Ajustes) usan datos de ejemplo de `PreviewData`. No son textos de la app: las pantallas reales leen todo de Compose Resources.
