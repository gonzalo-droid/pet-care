# Decisiones

Registro de decisiones tomadas durante el desarrollo que cambian o precisan `SYSTEM_DESIGN.md`. La más reciente va arriba. Cada una dice quién la tomó y en qué hito.

## 2026-11-13 · M3

### D29. M3 con fotos y sin lectura automática (opción C) `[Decidido por Gonzalo]`
Sin Firebase por ahora (D4), M3 hace todo lo que no depende de la tecnología de lectura: `ImagePicker`, `ImageProcessor`, guardado de fotos, Bienvenida (O1), fotos de la cartilla (O2), foto del sticker, foto de la mascota y visor. Quedan para cuando se elija la lectura: Leyendo (O3), Revisar lectura (O4), la cola sin internet y la hoja C4.
- Criterio de terminado ajustado: *con las fotos de la cartilla de Lilith, se registran sus 2 vacunas a mano viendo la foto; las fotos quedan en el detalle de cada vacuna y la foto de Lilith en su carnet, en ambas plataformas y sin internet.*
- `CardReader` queda declarado en el dominio, sin implementación.
- "Empezar con su cartilla" (O1) pide primero nombre y especie (sin lectura no hay de dónde sacarlos), luego las fotos y luego el formulario de vacuna con las páginas arriba. El botón dice "Empezar con su cartilla" y no "Escanear su cartilla" porque todavía no se escanea.
- "Agregar vacuna" abre las fotos de la cartilla, con "Prefiero llenarla a mano" para seguir sin fotos. Eso reemplaza la hoja C4 mientras no haya lectura.

### D30. Almacenamiento de fotos `[Propuesta]`
- Tabla `photos` (base v3, migración automática 2→3 probada). Una foto no se edita: se crea o se borra, por eso no lleva `updatedAt` ni `deletedAt` (excepción a la regla 8, igual que `Photo` en la sección 9).
- Archivo `photos/<id>.jpg` en el directorio privado (Android `filesDir`, iOS Application Support) y en la base solo la ruta relativa. Las pantallas pasan esa ruta a Coil y un `Mapper` la resuelve a la absoluta, que en iOS cambia entre instalaciones.
- La foto de la mascota también va en `photos`; `Pet.photoPath` guarda su ruta relativa.
- Las fotos se procesan y guardan apenas se eligen. Al abrir la app, `CleanUpPhotos` borra las que nadie usa (ni una vacuna ni una mascota) con más de 24 h; las de registros o mascotas borrados se conservan para poder restaurarlos y para el respaldo.

### D31. Permisos de cámara y fotos `[Propuesta]`
- Android: la app no declara el permiso de cámara, así `TakePicture` abre la cámara del sistema sin pedirlo. La galería usa el selector de fotos del sistema, que no pide permisos.
- iOS: la cámara pide permiso con el texto de `NSCameraUsageDescription` en español. La galería usa `PHPickerViewController`, que no pide permiso de fotos. Si se niega la cámara, la pantalla muestra una tarjeta con enlace a Ajustes y la galería sigue disponible.
- Las fotos se guardan sin metadatos: `ImageProcessor` vuelve a codificar el JPEG, así que no queda la ubicación.

### D32. Prueba del procesado de fotos en el emulador `[Propuesta]`
`Bitmap` y `ExifInterface` no existen en la JVM. `core/platform` tiene pruebas de dispositivo (`androidDeviceTest`) que el CI corre en el emulador antes de la prueba de avisos: rota según EXIF, reduce a 2000 px, conserva fotos chicas, quita la ubicación y rechaza lo que no es imagen. En iOS, el flujo de UI del simulador pasa fotos generadas por el `ImageProcessor` real.

## 2026-11-13 · M2

### D22. Salud muestra cuidados, no vacunas `[Decidido por Gonzalo]`
El prototipo lista la vacuna de leucemia dentro de Cuidados. En la app, Cuidados muestra solo desparasitaciones y otros cuidados; las vacunas siguen en Cartilla. Los avisos cubren ambos, e Inicio muestra lo que venga antes (vacuna o cuidado).

### D23. Permiso de avisos después del primer logro `[Decidido por Gonzalo]`
La pantalla O5 se muestra una sola vez: al guardar la primera vacuna con próxima fecha o el primer cuidado con aviso, y solo si el sistema todavía no preguntó. Si el permiso está denegado, Inicio y Salud muestran una tarjeta con enlace a los ajustes del sistema. O5 vive en `feature/onboarding`, que en M3 recibirá el resto del primer uso.

### D24. Sincronización de avisos `[Propuesta]`
- `ReminderEngine` (dominio, puro) calcula los avisos deseados: 7 días antes, 1 día antes y el mismo día a las 9:00, y "¿Ya le pusiste…?" a los 3 días; como máximo los 50 más cercanos.
- Una vacuna deja de avisar si hay un registro posterior de la misma mascota con algún tipo en común (ya se puso la siguiente dosis). Un cuidado se actualiza al cambiar su última fecha.
- `SyncReminders` compara con la tabla `scheduled_notifications` y solo agrega o cancela la diferencia. El ID del aviso incluye una firma del texto, así que renombrar la mascota reprograma.
- `ReminderCoordinator` sincroniza mientras la app está abierta, cuando cambian los datos o la hora preferida. En Android, `RescheduleReceiver` reprograma todo tras reiniciar o cambiar la hora; en iOS, `BGAppRefreshTask` cada ~12 h.
- Los textos de los avisos se arman en `core/platform` con Compose Resources al programarlos (regla 3).
- Hora preferida fija en 9:00 hasta que Ajustes (M4) permita cambiarla.

### D25. Cuidado sin aviso `[Propuesta]`
La última fecha y el intervalo son obligatorios solo si el aviso está activo: sin ellos no hay próxima fecha. Así se puede anotar un cuidado puntual sin aviso.

### D26. Herramienta de prueba de avisos `[Propuesta, se quita en M5]`
Ajustes tiene "Probar aviso en 1 minuto" para comprobar el criterio de M2 (llega con la app cerrada y tras reiniciar) sin esperar días. El aviso de prueba se guarda en DataStore y pasa por `SyncReminders` como los reales, así que también se reprograma al reiniciar. En el APK de debug, `DebugTestReminderReceiver` permite programarlo desde `adb`; el CI lo usa en la prueba del emulador (job "Android (avisos en emulador)").

### D28. Alarmas exactas antes de Android 12 `[Propuesta]`
Antes de Android 12, la ventana de una alarma no exacta es el 75 % del tiempo que falta: un aviso a 7 días podía llegar hasta 5 días tarde. En Android 8 a 11 se usa `setExactAndAllowWhileIdle`, que en esas versiones no pide permiso. Desde Android 12 sigue `setAndAllowWhileIdle` (sección 11), que el sistema entrega a lo más 1 hora después de la hora pedida.

### D27. Limitaciones conocidas de avisos `[Riesgo]`
Android puede retrasar varios minutos las alarmas no exactas en ahorro de batería, y algunas marcas (Xiaomi, Huawei, Oppo) las cortan si la app no está exenta de optimización de batería. iOS no garantiza el horario de `BGAppRefreshTask`. Ambos casos se revisan en la beta (M5).

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
