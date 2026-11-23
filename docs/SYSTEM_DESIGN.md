# System Design: BestID, cartilla digital de mascotas (MVP)

Versión 1.3 · 13 nov 2026 · Producto: Gonzalo · Diseño técnico: Claude
Cambios de la 1.3 (kickoff): token `outlineStrong`, `CareTask` con intervalo en días o meses, `ScanStatus`, estructura multimódulo, sin analítica por ahora. Detalle en `docs/DECISIONS.md`.
Estado: decisiones de producto y arquitectura cerradas para iniciar desarrollo. Las preguntas que quedan (sección 20) no bloquean el inicio y tienen un valor por defecto.

Enlaces:
- Prototipo visual (15 tableros, coincide con este documento, ver sección 6): https://claude.ai/artifact/SVop87MmxFfrJ1dY4CqSi3
- Plan de validación y MVP: https://claude.ai/artifact/GNAiGtQ6f6ikui33SsSvJa
- Ambos son privados hasta que Gonzalo los comparta. Este documento es suficiente sin ellos.

## 0. Cómo leer este documento

- Fuente de verdad del MVP. Si el prototipo y este documento difieren, gana este documento (la sección 6 lista las diferencias).
- Marcas: `[Decidido]` cerrado con Gonzalo. `[Propuesta]` decisión técnica razonable que el equipo puede cambiar con argumentos y avisando. `[Pendiente]` requiere respuesta de Gonzalo; usar el valor por defecto indicado mientras tanto.
- La UI está en español (es-PE). El código, los identificadores y los nombres técnicos van en inglés.
- Lectura mínima para empezar: secciones 1, 2, 4, 5, 8, 9, 10 y 17.

## 1. Producto

**Problema.** En Perú la cartilla de vacunación de las mascotas es de papel: stickers de frasco pegados, fechas escritas a mano y sellos encima. Se pierde, se lee mal y nadie avisa cuándo toca la siguiente dosis.

**Propuesta.** "Foto de la cartilla y te aviso cuándo toca." La app lee la cartilla con una foto, crea la mascota y su historial de vacunas, guarda la foto original y avisa las próximas dosis y desparasitaciones, incluso sin internet.

**Usuario.** Dueños de gatos y perros en Perú. Otras especies se pueden registrar, pero el catálogo de vacunas del MVP es de perros y gatos.

**Principios (no negociables)**

1. La cartilla es el gancho: el primer uso empieza escaneando.
2. La IA propone y la persona confirma. Nada leído se guarda sin confirmación, y un dato ilegible nunca se inventa.
3. La foto original se conserva siempre junto a los datos leídos.
4. Los avisos funcionan sin internet.
5. Tierno pero legible: colores suaves, contraste suficiente, botones grandes, la mascota siempre visible.
6. La app no da consejo veterinario. Solo registra lo que dice la cartilla o el dueño.

## 2. Decisiones cerradas

| Tema | Decisión | Estado |
|---|---|---|
| Plataformas | Android e iOS desde el lanzamiento | [Decidido] |
| Tecnología | Kotlin Multiplatform con interfaz compartida en Compose Multiplatform | [Decidido] |
| Datos | Solo en el teléfono. Sin cuenta, sin login, sin sincronización en el MVP | [Decidido] |
| Backend | Sin servidor propio. **Por ahora sin Firebase** (13 nov 2026): datos solo en el teléfono. La forma de leer la cartilla (Firebase AI Logic, servicio propio sin estado u otra) se decide antes de M3; ver sección 10 y `docs/DECISIONS.md` D4 | [Decidido, lectura pendiente] |
| Equipo | Gonzalo desarrolla | [Decidido] |
| Nombre | BestID ("bestie" + ID: la identidad de tu mascota). En tienda va con complemento: "BestID: cartilla de tu mascota". Riesgos: en español puede leerse "vestid"; existen "bestID Attendance" (app de asistencia, otra categoría) y un desarrollador "VestID" en Google Play. Revisar marca en INDECOPI antes de publicar (sección 20) | [Decidido, con riesgo] |
| Catálogo de vacunas por especie | Lo valida un veterinario antes del lanzamiento | [Decidido] |
| Respaldo | Exportar e importar un archivo de respaldo, más el respaldo del sistema operativo | [Decidido] |
| Primer uso | Empieza escaneando: la foto crea la mascota y sus vacunas | [Decidido] |
| Navegación | 4 pestañas: Inicio, Cartilla, Salud, Mascota. Compartir es un botón, no una pestaña | [Decidido] |
| Alcance del lanzamiento | Escaneo, vacunas a mano, cartilla, avisos, desparasitación y otros cuidados, peso con gráfico, compartir en PDF, varias mascotas | [Decidido] |
| Fuera del MVP | Cuenta y nube, enlace compartible, tema oscuro, citas, recetas, gastos, QR, panel para veterinarias, cobro | [Decidido] |

## 3. Historias de usuario

| ID | Historia | Hito |
|---|---|---|
| U1 | Como dueño, escaneo la cartilla y la app crea la mascota con sus vacunas para que yo confirme | M3 |
| U2 | Registro o corrijo una vacuna a mano eligiendo tipos de una lista o escribiendo uno nuevo | M1 |
| U3 | Veo el historial de vacunas de mi mascota y la foto original de cada registro | M1 |
| U4 | Recibo un aviso antes de cada próxima vacuna y desparasitación, sin internet | M2 |
| U5 | Registro desparasitaciones y otros cuidados con su intervalo | M2 |
| U6 | Registro el peso y veo su evolución en un gráfico | M2 |
| U7 | Tengo varias mascotas y cambio entre ellas desde cualquier pantalla | M1 |
| U8 | Envío la ficha en PDF por WhatsApp a la veterinaria | M4 |
| U9 | Exporto un respaldo y lo importo en otro teléfono | M4 |
| U10 | Edito o elimino una mascota o un registro, con opción de deshacer | M1 |

## 4. Navegación e inventario de pantallas

### Estructura

```
Onboarding (sin pestañas)
  O1 Bienvenida -> O2 Captura -> O3 Leyendo -> O4 Revisar lectura -> O5 Permiso de avisos -> Inicio
  O1 "Empezar sin cartilla" -> P2 Agregar mascota (mínimo) -> O5 -> Inicio

App (barra superior con selector de mascota + barra inferior de 4 pestañas)
  Inicio     -> Ajustes (ícono), Captura, Detalle de vacuna, Salud
  Cartilla   -> Detalle de vacuna -> Editar vacuna
             -> Agregar vacuna (Captura o A mano)
             -> Compartir (botón)
  Salud      -> segmento Cuidados | Peso
             -> Agregar o editar cuidado, Agregar peso
  Mascota    -> Editar mascota, Agregar mascota, Eliminar mascota
Ajustes      -> Respaldo (exportar, importar), Avisos, Privacidad, Acerca de
```

La barra superior muestra el selector de mascota (avatar y nombre, toca para cambiar o agregar) en las 4 pestañas. Todas las pestañas muestran datos solo de la mascota seleccionada. La selección se recuerda entre sesiones.

### Inventario

| ID | Pantalla | Ruta | Tipo | Contenido clave |
|---|---|---|---|---|
| O1 | Bienvenida | `onboarding/welcome` | Pantalla completa | Propuesta en una frase, ilustración, "Escanear cartilla" (principal), "Empezar sin cartilla" (secundario) |
| O2 | Captura | `scan/capture` | Flujo modal | Consejos de foto, "Tomar foto" (cámara del sistema), "Elegir de galería" (hasta 4 páginas), miniaturas de las páginas elegidas, "Leer cartilla" |
| O3 | Leyendo | `scan/reading` | Estado | Progreso, texto "Estamos leyendo la cartilla", cancelar. Errores: sin internet, no se pudo leer |
| O4 | Revisar lectura | `scan/review` | Flujo modal | Sección Mascota detectada (si es primer uso o no coincide) y lista de registros detectados, cada uno confirmable o descartable. Campos dudosos en durazno con ícono y la palabra "Revisa", resueltos con una pregunta o un campo. Aviso de paneles vacíos ignorados. "Guardar N vacunas" fijo al pie |
| O5 | Permiso de avisos | `onboarding/notifications` | Pantalla completa | Explica para qué son los avisos y luego pide el permiso del sistema. "Ahora no" permitido |
| H1 | Inicio | `home` | Pestaña | Tarjeta de mascota, próximo aviso con días restantes, última vacuna, último peso, botón "Escanear cartilla" |
| C1 | Cartilla | `cartilla` | Pestaña | Próxima dosis, lista de registros por fecha, botones "Agregar vacuna" y "Compartir" |
| C2 | Detalle de vacuna | `vaccine/{id}` | Detalle | Fecha, próxima fecha, tipos, productos con lote, elaboración y vencimiento, veterinaria, foto original y foto del sticker, editar, eliminar |
| C3 | Agregar o editar vacuna | `vaccine/edit?id=` | Formulario | Tipos filtrados por especie, "Otra" a mano, fecha, próxima fecha, y en "Más detalles" productos, lotes, veterinaria y foto del sticker |
| C4 | Agregar vacuna: elegir método | hoja inferior | Hoja | "Escanear cartilla" o "Agregar a mano" |
| S1 | Salud | `health?tab=care|weight` | Pestaña | Segmento Cuidados (lista con aviso activado o no) y Peso (gráfico, último registro, lista) |
| S2 | Agregar o editar cuidado | `care/edit?id=` | Formulario | Tipo (desparasitación interna, externa, otro), título si es otro, última fecha, intervalo en días o meses, aviso sí o no |
| S3 | Agregar peso | `weight/new` | Formulario | Fecha (hoy por defecto), peso en kg |
| M1 | Mascota | `pet` | Pestaña | Carnet BestID con foto, especie, sexo, raza, nacimiento, señas, tutor, número de vacunas y próxima fecha. Botones "Compartir carnet", "Completar datos" y "+ Agregar otra mascota" |
| M2 | Agregar o editar mascota | `pet/edit?id=` | Formulario | Obligatorios: nombre y especie. En "Más detalles": raza, sexo, fecha de nacimiento (aproximada permitida), señas particulares, propietario, dirección, foto |
| SH1 | Compartir ficha | `share` | Flujo modal | Vista previa, casillas de qué incluir, "Compartir PDF" (hoja del sistema, incluye WhatsApp) |
| A1 | Ajustes | `settings` | Detalle | Respaldo, avisos (hora del aviso), privacidad, acerca de, versión |
| A2 | Respaldo | `settings/backup` | Detalle | "Exportar respaldo", "Importar respaldo", fecha del último respaldo exportado |

### Estados obligatorios en cada pantalla que aplique

Vacío (sin mascotas, sin vacunas, sin pesos, sin cuidados), cargando, error con acción para reintentar, sin internet (solo afecta al escaneo), permiso de cámara o de notificaciones denegado con explicación y enlace a ajustes del sistema, confirmación de eliminación y snackbar "Deshacer" durante 5 segundos.

## 5. Flujos detallados

**F1. Primer uso escaneando**
1. O1 → "Escanear cartilla".
2. O2: la persona toma una o más fotos (una por página) o las elige de la galería.
3. O3: se envían las imágenes al servicio de lectura. Si no hay internet: mensaje con dos salidas, "Reintentar" o "Guardar fotos y continuar a mano" (las fotos quedan en una cola local y se ofrecen para leer cuando haya red).
4. O4: arriba, "Mascota detectada" con nombre, especie, sexo y propietario prellenados y editables. Abajo, un bloque por cada registro de vacuna detectado. Cada bloque tiene un interruptor "Incluir" (encendido por defecto si tiene fecha de aplicación). Los paneles vacíos no aparecen. Los campos dudosos se ven en durazno.
5. "Guardar": crea la mascota, los registros confirmados y adjunta las fotos originales a cada registro.
6. O5: explicación y permiso de notificaciones. Se programan los avisos.
7. Inicio con la mascota creada.

**F2. Escanear con mascotas ya creadas**
Igual que F1 pero desde Cartilla o Inicio. En O4 los registros se asignan a la mascota seleccionada. Si el nombre leído no coincide con la mascota seleccionada, O4 muestra un aviso con opción de cambiar de mascota o crear una nueva.

**F3. Vacuna a mano**
Cartilla → "Agregar vacuna" → "Agregar a mano" → C3 → Guardar → C1. Obligatorio: al menos un tipo (o uno escrito a mano) y la fecha de aplicación.

**F4. Cuidados y peso**
Salud → segmento → "+" → S2 o S3 → Guardar → S1. Guardar un cuidado con aviso activado programa los avisos.

**F5. Compartir**
Cartilla o Mascota → "Compartir" → SH1 → "Compartir PDF" → hoja del sistema. Por defecto incluye datos de la mascota y vacunas con lote y fecha. No incluye dirección ni propietario salvo que se marquen.

**F6. Respaldo**
Ajustes → Respaldo → "Exportar" genera un archivo `.zip` y abre la hoja del sistema (Drive, Archivos, WhatsApp a uno mismo). "Importar" abre el selector de archivos, valida y combina por ID; si un registro existe en ambos lados gana el `updatedAt` más reciente.

## 6. Prototipo y decisiones de diseño

El prototipo tiene 15 tableros en 3 filas y coincide con este documento:
- **Primer uso:** Bienvenida, Fotos de la cartilla, Leyendo, Revisar lectura, Permiso de avisos.
- **App de 4 pestañas:** Inicio, Cartilla, Detalle de vacuna, Salud (Cuidados y Peso), Mascota.
- **Formularios y componentes:** Nueva vacuna, Nueva mascota, Compartir ficha, Componentes y tokens.

Decisiones de diseño `[Decidido]`:
1. **Inicio con tarjeta:** la mascota en grande, el próximo aviso con los días que faltan en número grande y la acción para confirmar la fecha si es dudosa.
2. **Revisar lectura en lista:** todas las vacunas detectadas en una pantalla, con "Incluir" por registro y un solo botón "Guardar N vacunas" fijo al pie. Los datos dudosos se preguntan en concreto, por ejemplo "¿Le pusieron la antirrábica? Sí, se la pusieron / No estoy seguro", o con un campo para el año tapado.
3. **Mascota como carnet BestID:** la pestaña Mascota muestra un carnet (encabezado verde con el logo, foto, datos y franja con vacunas y próxima fecha) que se comparte como identidad.
4. **Permiso de avisos después del primer logro:** primero confirma "Lilith y sus 2 vacunas guardadas" y muestra un ejemplo del aviso; recién después pide el permiso.

Lo que el prototipo no muestra y se construye con los mismos componentes:
- Ajustes, Respaldo, Agregar peso, Agregar o editar cuidado, la hoja "Agregar vacuna: escanear o a mano", el visor de foto y los estados vacío, error y sin permiso.
- En la app real la cámara y la galería son las del sistema, las fechas usan el selector nativo y los días restantes se calculan.

## 7. Design system

### Tokens de color `[Decidido]`

| Token | Valor | Uso |
|---|---|---|
| bg | #FBF6EC | Fondo de pantallas |
| surface | #FFFFFF | Tarjetas y campos |
| ink | #2B3A36 | Texto principal |
| muted | #5A6B65 | Texto secundario |
| line | #EADFCB | Bordes |
| track | #EFE6D3 | Fondo del control segmentado |
| outline | #CDBF9F | Separadores decorativos (1,82:1 sobre surface, no delimita controles) |
| outlineStrong | #A48C55 | Borde de controles: botón secundario y punteado, campos, chips (3,25:1 sobre surface) |
| primary | #2F7D68 | Acciones, elemento activo |
| onPrimary | #FFFFFF | Texto sobre primary |
| primarySoft | #DDF1E8 | Estado correcto, selección |
| primaryInk | #1F5E4D | Texto sobre primarySoft |
| alert | #FFE0CC | Avisos y datos por confirmar |
| alertInk | #7A3B1B | Texto sobre alert |
| error | #B3261E | Errores de formulario |

Contraste verificado con la fórmula WCAG (prueba `ContrastTest`): onPrimary sobre primary 4,94:1; muted sobre bg 5,23:1; primary sobre bg 4,58:1; outlineStrong sobre surface 3,25:1. El texto no seleccionado del control segmentado usa ink. El color nunca es la única señal: los avisos llevan texto y los estados llevan etiqueta.

Tema oscuro: fuera del MVP. La app fuerza tema claro en ambas plataformas `[Decidido]`.

### Tipografía, forma y espacio

- Fuente: Nunito (500, 700, 800), incluida en la app con Compose Resources. No se descarga.
- Escala: display 32/800, título 26/800, subtítulo 20/800, tarjeta 18/800, cuerpo 16/700, apoyo 14/700, etiqueta de barra 13/700. Ningún texto de la app baja de 13.
- Respeta la escala de texto del sistema hasta 200 % sin cortar contenido (usar `sp` y permitir que las tarjetas crezcan).
- Radios: tarjetas 24 a 28, campos 18, botones 24, chips y badges completamente redondeados.
- Espaciado base de 4: 4, 8, 12, 16, 20, 24, 32. Margen lateral de pantalla 20.
- Alto táctil mínimo 48 dp. Botón principal 58, secundario 52.
- Íconos: Material Symbols Rounded, trazo 2, tamaño 24 `[Propuesta]`.
- Ilustraciones: avatar genérico de gato y perro cuando no hay foto `[Pendiente]` encargar ilustraciones propias; usar placeholders simples mientras tanto.

### Componentes (módulo `designsystem`)

Base: Material 3 de Compose Multiplatform con un `ColorScheme`, `Typography` y `Shapes` propios construidos con los tokens. Ninguna pantalla define colores, tamaños o radios sueltos.

Elevación (D44): las tarjetas de superficie (`PlainCard` y las que se construyen sobre ella, y `PetIdCard`), el `PetSelector` y el `IconAction` usan una sombra suave (`BestIdElevation.card`, 6 dp, color `shadow`: ink al 18 %) en lugar del borde de 2 dp. Los controles (botón secundario y punteado, campos, chips) conservan el borde `outlineStrong`, porque delimitan algo que se toca. El durazno (`alert`) marca solo el dato dudoso.

| Componente | Props principales | Notas |
|---|---|---|
| AppTheme | content | Aplica tokens, fuente y formas |
| MainScaffold | selectedTab, petSelector, content | Barra superior con selector y barra inferior de 4 pestañas |
| FlowScaffold | title, onClose, content, bottomAction | Para flujos modales y formularios; botón principal fijo al pie |
| PetSelector | pets, selectedId, onSelect, onAdd | Avatar y nombre; abre hoja con la lista |
| BottomBar | selected, onSelect | Inicio, Cartilla, Salud, Mascota |
| PrimaryButton, SecondaryButton, DashedButton, TextAction | text, onClick, icon?, loading? | Estados deshabilitado y cargando |
| FormField | label, value, onValueChange, placeholder, helper?, error?, optional?, keyboardType | Texto, número, teléfono, multilínea |
| DateField | label, value, onChange, allowApproximate? | Abre selector nativo de fecha |
| SelectField | label, options, selected | Para especie y tipo de cuidado |
| ChipToggle | label, selected, onToggle | Para tipos de vacuna (multiselección) |
| ChipChoice | label, selected, onSelect | Para sexo (selección única) |
| SegmentedControl | options, selected | Para Salud (Cuidados, Peso) |
| SwitchRow | title, subtitle, checked | Para activar avisos de un cuidado |
| ExpandableSection | title, content | "Más detalles" |
| InfoCard, AlertCard, PlainCard | content | Fondo primarySoft, alert o surface |
| StatCard | label, value, action? | Última vacuna, último peso |
| VaccineRecordCard | record, onClick | Fecha, productos, chips de tipos, veterinaria |
| DateTile | day, month, contentDescription, review? | Mosaico de día y mes; primarySoft, o alert con review cuando la fecha está por confirmar. Crece con la letra (D44) |
| CountdownRing | value, unit, progress? | Anillo de cuenta regresiva de Inicio; crece con la letra y se lee junto al texto que acompaña |
| ReviewStrip | text, actionText, onAction | Franja alert con ícono de advertencia (Warning) para un dato dudoso; toque de 48 dp o más |
| TimelineItem | date, content | Riel con mosaico de fecha a la izquierda y la tarjeta al lado; con letra grande se apila (D43) |
| ProgressRow | text, progress, actionText, onClick | Barra de avance con acción ("Faltan raza y tutor · Completar") en el carnet |
| ReviewRecordBlock | draft, onChange, included, onToggleIncluded | Bloque de O4 con campos dudosos en durazno |
| ListRow | label, value | Dos columnas |
| Badge | text, tone (ok, alert, neutral) | |
| PetAvatar | photo?, species, size | Placeholder por especie |
| PetIdCard | name, photo, species, subtitle, details, vaccineCount, nextDate, completion? | Carnet de la pestaña Mascota (encabezado con trama, avatar montado, datos llenos y ProgressRow si faltan, D44) y base de la ficha en PDF |
| ReviewField | reason, content | Fondo alert, ícono y la palabra "Revisa"; contiene una pregunta (ChipChoice) o un campo |
| NotificationPreview | title, body | Ejemplo de aviso en la pantalla de permiso |
| CelebrationBadge | text | Confirmación tierna tras guardar ("Lilith y sus 2 vacunas guardadas") |
| WeightChart | points | Dibujado con Canvas: línea, área suave, punto final destacado, ejes rotulados en kg y meses |
| PhotoThumb | image, onClick | Abre visor de foto a pantalla completa |
| EmptyState | illustration, title, body, action | |
| LoadingState, ErrorState | text, onRetry? | |
| PermissionCard | title, body, onOpenSettings | |
| ConfirmSheet | title, body, confirmText, destructive | Para eliminar |
| UndoSnackbar | message, onUndo | 5 segundos |

Criterio de terminado de M0: cada componente tiene preview en estados normal, deshabilitado, error y texto largo, y coincide visualmente con el prototipo.

## 8. Arquitectura (Kotlin Multiplatform)

### Stack `[Propuesta]` (usar las últimas versiones estables al iniciar)

| Necesidad | Librería |
|---|---|
| Lenguaje y build | Kotlin 2.x, Gradle con version catalog y plugins de convención (`build-logic`), plantilla oficial de KMP con Compose Multiplatform (AGP 9: módulo `androidApp` separado) |
| UI compartida | Compose Multiplatform + Material 3 + Compose Resources (fuente, textos, imágenes) |
| Navegación | Navigation Compose multiplataforma de JetBrains con rutas tipadas |
| Estado | ViewModel y Lifecycle multiplataforma, StateFlow, flujo unidireccional |
| Inyección | Koin (Hilt no soporta KMP) |
| Base de datos | Room KMP con el driver SQLite empaquetado |
| Preferencias | DataStore KMP |
| Red | Ktor client (motor OkHttp en Android, Darwin en iOS) + kotlinx.serialization, para la alternativa de servicio propio |
| Fechas | kotlinx-datetime |
| Imágenes | Coil 3 |
| Errores | Sentry Kotlin Multiplatform |
| Pruebas | kotlin.test, Turbine, coroutines-test, Room en memoria |

### Código por plataforma (expect/actual o interfaces con implementación por plataforma)

| Interfaz común | Android | iOS |
|---|---|---|
| `ImagePicker` (tomar foto, elegir hasta 4) | ActivityResult `TakePicture` y Photo Picker | `UIImagePickerController` y `PHPickerViewController` |
| `ImageProcessor` (rotar, reducir a ~2000 px de lado largo, JPEG 85, quitar metadatos) | Bitmap + ExifInterface | UIImage + `jpegData` |
| `NotificationScheduler` (programar, cancelar, pedir permiso) | AlarmManager no exacto + NotificationCompat + receptores de reinicio y cambio de hora | `UNUserNotificationCenter` con `UNCalendarNotificationTrigger` |
| `PdfRenderer` (ficha de la mascota) | `PdfDocument` | `UIGraphicsPDFRenderer` |
| `ShareSheet` (compartir archivo) | `Intent.ACTION_SEND` con FileProvider | `UIActivityViewController` |
| `FilePicker` (importar respaldo) | Storage Access Framework | `UIDocumentPickerViewController` |
| `CardReader` (enviar fotos y recibir JSON) | SDK de Firebase AI Logic para Android | SDK de Firebase AI Logic para iOS (Swift), expuesto a Kotlin con una interfaz |
| Verificación de la app (App Check) | Play Integrity | App Attest |

Toda la lógica (qué avisos existen, cuándo, con qué texto) vive en código común. Las implementaciones por plataforma solo ejecutan.

### Estructura de módulos `[Propuesta]`

```
/composeApp        UI compartida y punto de entrada Android
  commonMain/  designsystem/  navigation/  feature/{onboarding,home,cartilla,scan,health,pet,share,settings}/
  androidMain/ iosMain/       implementaciones de plataforma
/core
  model/        entidades y enums
  domain/       casos de uso, motor de avisos, validaciones (100 % común, con pruebas)
  data/         Room, DAOs, repositorios, respaldo, cliente del servicio de lectura
/iosApp            proyecto Xcode que embebe el framework
/backend/scan      solo si se cambia a un servicio propio (sección 10)
```

Estructura vigente: ver `docs/DECISIONS.md` (D1). Multimódulo desde M0 (`androidApp`, `composeApp`, `core/*`, `feature/*`); `core/domain`, `core/data` y `core/platform` se crean en el hito que los necesita. Las interfaces de plataforma son puertos en `domain` con adaptadores Android e iOS en `core/platform`. Lo obligatorio es que `domain` no dependa de UI ni de plataforma.

### Capas

```
Compose UI -> ViewModel (StateFlow<UiState>, eventos) -> UseCase -> Repository -> Room
                                                      -> ScanClient (Ktor) -> backend/scan
ReminderEngine (común, puro) -> NotificationScheduler (por plataforma)
```

Reglas: la UI solo observa estado y envía eventos. Toda escritura pasa por un caso de uso. Los IDs son UUID en texto y cada entidad tiene `createdAt`, `updatedAt` y `deletedAt` (borrado lógico), para que el respaldo y una futura sincronización sean simples.

## 9. Modelo de datos

```kotlin
enum class Species { DOG, CAT, REPTILE, BIRD, SMALL_MAMMAL, OTHER }
enum class Sex { FEMALE, MALE }
enum class DatePrecision { EXACT, MONTH, YEAR }
enum class RecordSource { SCAN, MANUAL }
enum class CareKind { DEWORM_INTERNAL, DEWORM_EXTERNAL, OTHER }
enum class IntervalUnit { DAY, MONTH }
enum class ScanStatus { PENDING, READING, DONE, FAILED, CANCELLED }   // [Propuesta], se revisa en M3

data class Pet(
  val id: String, val name: String, val species: Species,
  val breed: String?, val sex: Sex?,
  val birthDate: LocalDate?, val birthDatePrecision: DatePrecision?,
  val distinguishingMarks: String?,          // "mancha blanca en la cola"
  val ownerName: String?, val ownerAddress: String?,
  val photoPath: String?,
  val createdAt: Instant, val updatedAt: Instant, val deletedAt: Instant?
)

data class Vet(                               // se autocompleta en formularios
  val id: String, val name: String, val contact: String?,   // teléfono o WhatsApp
  val createdAt: Instant, val updatedAt: Instant, val deletedAt: Instant?
)

data class VaccineType(                       // catálogo semilla + tipos escritos a mano
  val id: String,                             // "calicivirus" o "custom:<uuid>"
  val label: String, val species: Set<Species>, val isCustom: Boolean
)

data class VaccineRecord(
  val id: String, val petId: String,
  val applicationDate: LocalDate,
  val nextDate: LocalDate?, val nextDateConfirmed: Boolean,
  val nextDateLabel: String?,                 // "Próx. leucemia"
  val typeIds: List<String>,
  val typeIdsToConfirm: List<String>,         // ej. sticker de rabia con casilla sin marcar
  val items: List<VaccineItem>,
  val vetId: String?,
  val source: RecordSource,
  val sourcePhotoIds: List<String>,           // páginas de la cartilla
  val stickerPhotoId: String?,
  val extraction: ExtractionMeta?,            // confianza por campo y versión del modelo
  val createdAt: Instant, val updatedAt: Instant, val deletedAt: Instant?
)

data class VaccineItem(                       // un sticker de frasco
  val productName: String?, val lot: String?,
  val manufactureDate: LocalDate?, val expiryDate: LocalDate?
)

data class CareTask(
  val id: String, val petId: String, val kind: CareKind, val title: String?,
  val lastDate: LocalDate?,
  val intervalValue: Int?, val intervalUnit: IntervalUnit?,   // DAY o MONTH (meses de calendario)
  val nextDate: LocalDate?,
  val reminderEnabled: Boolean,
  val createdAt: Instant, val updatedAt: Instant, val deletedAt: Instant?
)

data class WeightEntry(
  val id: String, val petId: String, val date: LocalDate, val grams: Int,   // en gramos para evitar decimales
  val createdAt: Instant, val updatedAt: Instant, val deletedAt: Instant?
)

data class Photo(val id: String, val path: String, val width: Int, val height: Int, val createdAt: Instant)

data class ScanJob(                           // cola local de fotos pendientes de leer
  val id: String, val petId: String?, val photoIds: List<String>,
  val status: ScanStatus, val error: String?, val createdAt: Instant
)

data class ScheduledNotification(             // lo programado, para comparar y reprogramar
  val id: String, val refType: String, val refId: String, val fireAt: Instant
)
```

Las listas (`typeIds`, `items`, `sourcePhotoIds`) se guardan como JSON en Room con un TypeConverter, o como tablas hijas si el equipo lo prefiere. Las fotos se guardan en el directorio privado de la app (en iOS, Application Support) y en la base solo va la ruta relativa.

**Catálogo semilla de VaccineType** (sale de las cartillas vistas): parvovirus, distemper, hepatitis, leptospirosis, parainfluenza, coronavirus, kc (tos de perreras), rabia, calicivirus, rinotraqueitis, panleucopenia, leucemia.
Especie propuesta: perro para los siete primeros, gato para calicivirus, rinotraqueitis, panleucopenia y leucemia, ambos para rabia. Esta asignación la propuso Claude porque las cartillas listan todo junto. Gonzalo la valida con un veterinario antes del lanzamiento `[Decidido]`. Mientras tanto, C3 filtra por especie y tiene "Ver todas", así que un error de asignación no bloquea a nadie.

**Reglas de validación**
- Fecha de aplicación no futura. Próxima fecha posterior a la aplicación.
- Vencimiento posterior a elaboración. Si un sticker vence antes de la fecha de aplicación, se avisa en durazno pero se permite guardar.
- Peso entre 0,05 kg y 150 kg.
- Nombre de mascota de 1 a 40 caracteres.

## 10. Lectura de la cartilla

### Cliente
1. `ImagePicker` entrega hasta 4 imágenes. `ImageProcessor` las rota, reduce y quita metadatos (incluida la ubicación).
2. Se guardan como `Photo` y se crea un `ScanJob`.
3. `CardReader` las envía al modelo con las instrucciones y el esquema de respuesta.
4. La respuesta se valida en común (sección 9) y se muestra en O4. Nada se guarda hasta "Guardar".

### Opción elegida: Firebase AI Logic con Gemini `[Propuesta]`
- La app llama a Gemini directamente con el SDK de Firebase AI Logic, sin servidor propio. La clave no vive en la app: Firebase la gestiona.
- Firebase AI Logic tiene SDK oficial para Android (Kotlin) e iOS (Swift), pero no para KMP. Se usa detrás de la interfaz común `CardReader`, con una implementación por plataforma. Existe una librería KMP de la comunidad (`firebase-ai-kmp`), pero es experimental (versión 0.4) y no se usa en producción.
- App Check es obligatorio: desde el 2 de noviembre de 2026 Firebase lo exige para usar Firebase AI Logic. Android usa Play Integrity e iOS usa App Attest.
- Se pide respuesta en JSON con el esquema de abajo (salida estructurada del modelo).
- Las instrucciones y el esquema viajan dentro de la app. Cambiarlos requiere publicar una versión, salvo que se carguen desde Remote Config `[Propuesta para después del lanzamiento]`.
- Costo: Firebase AI Logic no cobra. Se paga el uso de Gemini. El nivel gratuito sirve para desarrollo y para la prueba de 10 cartillas. Con usuarios reales, usar el nivel de pago (ver sección 14).
- Control de gasto: cuotas y alertas de presupuesto en el proyecto de Google Cloud, y límite local de 20 escaneos por día por instalación.
- Modelo: elegir el modelo de Gemini con visión que dé mejor exactitud y costo en la prueba de 10 cartillas.

### Alternativa: servicio propio sin estado
Se usa si Gemini no alcanza la exactitud necesaria o si se quiere cambiar de proveedor sin publicar la app.
- Un endpoint `POST /v1/scan` (multipart, hasta 4 imágenes, máximo 4 MB cada una) en Cloud Run, que llama al proveedor elegido con su SDK oficial de servidor y responde el mismo JSON.
- Sin estado: no guarda imágenes ni resultados.
- Protección con el mismo App Check, límite por instalación y tope de gasto.
- Gracias a `CardReader`, la app solo cambia la implementación; las pantallas no cambian.

### Lectura en el teléfono sin IA (descartada como lector principal)
ML Kit Text Recognition (Android e iOS) y Apple Vision leen texto sin internet y gratis, pero devuelven texto suelto. No entienden casillas marcadas, stickers ni sellos, y leen mal las fechas escritas a mano. Se pueden usar después para avisar antes de enviar si la foto está borrosa o no tiene texto.

### Respuesta esperada (esquema JSON)

```json
{
  "pet": {
    "name": {"value": "Lilith", "confidence": 0.97},
    "species": {"value": "CAT", "confidence": 0.9},
    "sex": {"value": "FEMALE", "confidence": 0.9},
    "ownerName": {"value": "Tutor de Prueba", "confidence": 0.85}
  },
  "records": [{
    "page": 1,
    "applicationDate": {"value": "2025-12-21", "confidence": 0.95},
    "nextDate": {"value": null, "confidence": 0.2, "note": "último dígito del año tapado por un sello"},
    "nextDateLabel": "Próx. leucemia",
    "types": [{"id": "calicivirus", "checked": true, "confidence": 0.9}],
    "typesToConfirm": [{"id": "rabia", "reason": "sticker pegado, casilla sin marcar"}],
    "items": [{"productName": "Felocell 3", "lot": "774233A", "manufactureDate": "2024-11-08", "expiryDate": "2026-05-05"}],
    "vet": {"name": null, "contact": null},
    "signed": true
  }],
  "ignoredPanels": [{"page": 1, "reason": "panel sin fecha, firma ni casillas"}],
  "modelVersion": "<proveedor>/<modelo>/<fecha del prompt>"
}
```

### Instrucciones para el modelo (resumen del prompt)
- Extrae solo lo que se ve. Si algo no se lee, devuelve `null` con una nota. Nunca completes un dígito.
- Un panel sin fecha, firma ni casillas marcadas va a `ignoredPanels`.
- Un sticker de vacuna con su casilla sin marcar va a `typesToConfirm`, no a `types`.
- Fechas en ISO. Si el formato es ambiguo (día y mes), usa el orden peruano día/mes/año y baja la confianza.
- Tipos solo del catálogo; lo que no está, como texto en `types` con `id: "custom"` y su etiqueta.

### Revisión (O4)
- Un campo va en durazno si su confianza es menor a 0,8 `[Propuesta, ajustar con las pruebas]`, si tiene nota, o si falló una validación.
- Si `nextDate` es nula o dudosa, el registro se guarda con `nextDateConfirmed = false` y no programa aviso hasta que el dueño la complete.
- Se registran (sin contenido) cuántos campos corrigió la persona, para medir la exactitud real.

## 11. Avisos

### Motor común (`ReminderEngine`)
Función pura que, a partir de las vacunas con `nextDateConfirmed`, los cuidados con aviso activo y la hora preferida, produce la lista de avisos futuros:
- 7 días antes, 1 día antes y el mismo día, a la hora preferida (9:00 por defecto) `[Propuesta]`.
- Texto: "A {nombre} le toca su {vacuna o cuidado} en {n} días", "Mañana le toca su {…} a {nombre}", "Hoy le toca su {…} a {nombre}".
- Cuidados: `nextDate = lastDate + intervalValue` en días o en meses de calendario según `intervalUnit`. La app no sugiere intervalos; los pone el dueño `[Decidido]`.
- Pasada la fecha sin registrar nada: un aviso "¿Ya le pusiste su {…} a {nombre}?" a los 3 días, que abre el registro para marcarlo hecho `[Propuesta]`.

### Programación por plataforma
- `NotificationScheduler.sync(desired)` compara con `ScheduledNotification` y solo agrega o cancela la diferencia.
- Se llama después de cada cambio de datos, al abrir la app, al importar un respaldo y al cambiar la hora preferida.
- iOS: máximo 64 avisos pendientes por app. Se programan solo los 50 más cercanos de todas las mascotas y se vuelve a sincronizar al abrir la app y en `BGAppRefreshTask`.
- Android: alarmas no exactas (`setAndAllowWhileIdle`), porque Android 14 o superior no concede alarmas exactas por defecto. Reprogramar con `BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED` y `MY_PACKAGE_REPLACED`.
- Permiso: Android 13 o superior y iOS piden permiso. Se pide en O5 después de explicar para qué sirve, nunca al abrir la app.
- Tocar un aviso abre el registro correspondiente.

## 12. Compartir en PDF

- Página A4 vertical: encabezado con nombre de la app, foto y datos de la mascota, tabla de vacunas (fecha, tipos, producto, lote, vencimiento, veterinaria), próximas fechas y, opcionalmente, historial de peso y fotos de la cartilla en páginas siguientes.
- Pie: "Generado por la app el {fecha}. Los datos fueron registrados por el dueño; verificar con la cartilla original."
- No incluye dirección ni propietario salvo que se marquen.
- Se genera en el teléfono, sin internet.

## 13. Respaldo

- Archivo `.zip` con `manifest.json` (versión del esquema, fecha, versión de la app), `data.json` (todas las entidades, incluidas las borradas lógicamente) y la carpeta `photos/`.
- Importar: valida la versión del esquema, combina por ID (gana el `updatedAt` más reciente), copia fotos, reprograma avisos y muestra un resumen ("Se importaron 2 mascotas y 5 vacunas").
- Respaldo del sistema: Android Auto Backup incluye la base y las fotos (Android limita este respaldo a 25 MB por app, así que el respaldo manual es el que garantiza las fotos). En iOS, el respaldo del dispositivo incluye los datos de la app.
- Ajustes muestra la fecha del último respaldo exportado y sugiere exportar después de un escaneo `[Propuesta]`.

## 14. Privacidad, seguridad y tiendas

- Sin cuenta ni nube: los datos quedan en el teléfono. Las fotos solo salen para leerlas con Gemini a través de Firebase AI Logic.
- Con usuarios reales, usar el nivel de pago de la Gemini Developer API. Según los términos de Google, el contenido enviado en el nivel gratuito puede usarse para mejorar sus productos `[Verificar en los términos vigentes antes del lanzamiento]`.
- La política de privacidad debe decir que las fotos se envían a Google para leerlas y que la app no tiene servidor propio que las guarde.
- Datos personales: nombre y dirección del propietario, ambos opcionales.
- Sin datos personales en logs, en Sentry ni en analítica (configurar Sentry para no enviar PII ni capturas de pantalla).
- Sin cuenta, no aplica la obligación de las tiendas de borrar cuenta. Ajustes incluye "Borrar todos los datos".
- Google Play: completar la sección de seguridad de datos (fotos enviadas para procesamiento, no guardadas).
- App Store: etiquetas de privacidad, `PrivacyInfo.xcprivacy` y textos de permiso de cámara y fotos en español.
- `[Pendiente]` revisar con asesoría las obligaciones de la ley peruana de protección de datos personales y redactar la política de privacidad.
- Texto legal en Acerca de: la app no reemplaza la opinión del veterinario.

## 15. Accesibilidad y localización

- Todo elemento interactivo con descripción para TalkBack y VoiceOver; íconos decorativos ocultos.
- Orden de foco lógico; los campos con error anuncian el error.
- Texto hasta 200 % sin cortes. Probar la barra inferior con texto grande.
- Idioma es-PE con Compose Resources. Fechas dd/MM/aaaa, peso en kg con coma decimal, días con plural correcto ("1 día", "2 días").

## 16. Métricas

`[Decidido]` Sin analítica por ahora (13 nov 2026). Cuando se retome: mínima y anónima, eventos con un ID de instalación aleatorio a una herramienta que soporte KMP (PostHog tiene SDK para KMP). No hay backend al que enviarlos.

Eventos: `onboarding_started`, `scan_started`, `scan_completed` (páginas, registros detectados), `scan_fields_corrected` (cantidad), `vaccine_saved` (origen), `reminder_permission` (sí o no), `reminder_opened`, `care_saved`, `weight_saved`, `pdf_shared`, `backup_exported`. Nunca nombres, fechas ni fotos.

Métricas del lanzamiento: exactitud de lectura (campos sin corregir sobre total), porcentaje que activa avisos, porcentaje que vuelve a abrir la app en la semana 4.

## 17. Plan de implementación

Antes de M0 y en paralelo: ejecutar la prueba de lectura del plan de validación con 10 cartillas reales y dos proveedores. Si la exactitud es baja, el MVP sigue funcionando a mano y el escaneo se lanza como beta.

| Hito | Contenido | Terminado cuando |
|---|---|---|
| M0 | Proyecto KMP, módulos, design system con previews, navegación vacía con 4 pestañas, CI que compila Android e iOS, Sentry | La app abre en ambas plataformas con las pestañas y todos los componentes tienen preview |
| M1 | Mascotas (crear mínimo, editar, eliminar, varias, selector), vacunas a mano, Cartilla, Detalle, Inicio | Se registra a Lilith con sus 2 vacunas a mano en ambas plataformas, sin internet, y se cambia entre 2 mascotas |
| M2 | Motor de avisos, programación por plataforma, O5, Salud (cuidados y peso con gráfico) | Los avisos llegan con la app cerrada y tras reiniciar el teléfono en ambas plataformas; pruebas del motor en común |
| M3 | Lectura con Firebase AI Logic y App Check, captura, Leyendo, Revisar lectura con varios registros, primer uso escaneando, cola sin internet | Con las fotos de la cartilla de Lilith se obtiene lo de la sección 18, con los campos dudosos en durazno y el panel vacío ignorado |
| M4 | PDF y compartir, respaldo exportar e importar, Ajustes | El PDF llega por WhatsApp sin dirección por defecto; un respaldo exportado en Android se importa en iOS y al revés |
| M5 | Estados vacíos y de error, accesibilidad, textos finales, fichas de tienda, beta cerrada (prueba interna de Google Play y TestFlight) con 10 dueños | Sin errores bloqueantes en una semana de beta y métricas de la sección 16 registrándose |

Cada hito incluye pruebas en `commonTest` del dominio (validaciones, motor de avisos, combinación del respaldo) y una prueba manual de los flujos en un teléfono real de cada plataforma.

### Infraestructura del equipo
- Repositorio con CI (por ejemplo GitHub Actions): Android en Linux, iOS en un runner macOS.
- Distribución de pruebas: prueba interna de Google Play o Firebase App Distribution para Android, TestFlight para iOS.
- Cuentas: Google Play Console y Apple Developer Program (necesita una Mac para compilar iOS).
- Proyecto de Firebase con App Check y Firebase AI Logic, vinculado a facturación cuando haya usuarios reales.
- Desarrolla Gonzalo solo. Orden recomendado: la prueba de 10 cartillas primero (valida el gancho), luego M0 a M5 en orden. Si el tiempo aprieta, lo que se puede pasar a una versión 1.1 sin romper el producto es: peso, PDF y respaldo, en ese orden.

## 18. Datos de prueba: cartilla real de Lilith

Datos de una cartilla real con el nombre del tutor anonimizado. Las fotos originales no van al repositorio: Gonzalo las pasa aparte para las pruebas locales. Gonzalo confirmó estos datos, salvo el año de la próxima fecha.

```json
{
  "pet": {"name": "Lilith", "species": "CAT", "sex": "FEMALE", "ownerName": "Tutor de Prueba"},
  "records": [
    {"applicationDate": "2024-06-29", "signed": true,
     "types": ["rinotraqueitis", "panleucopenia", "calicivirus"],
     "items": [
       {"productName": "Felocell 3", "lot": "669022", "manufactureDate": "2023-05-11", "expiryDate": "2024-11-05"},
       {"productName": "Diluente estéril Felocell 3", "lot": "675043", "manufactureDate": "2023-04-24", "expiryDate": "2024-11-05"}]},
    {"applicationDate": "2025-12-21", "signed": true,
     "types": ["rinotraqueitis", "panleucopenia", "calicivirus"],
     "typesToConfirm": ["rabia"],
     "items": [
       {"productName": "Felocell 3", "lot": "774233A", "manufactureDate": "2024-11-08", "expiryDate": "2026-05-05"},
       {"productName": "Diluente estéril Felocell 3", "lot": "775507", "manufactureDate": "2024-11-26", "expiryDate": "2026-05-05"},
       {"productName": "Defensor 3 (antirrábica)", "lot": "766517", "manufactureDate": "2024-09-19", "expiryDate": "2026-03-18"}]}
  ],
  "nextDate": {"label": "Próx. leucemia", "raw": "31-12-202?", "expected": null,
               "note": "Último dígito del año tapado por un sello. La lectura debe devolver null y pedirlo."},
  "ignoredPanels": [{"reason": "Felocell 3 lote 705446C y diluente 706414A sin fecha, firma ni casillas"}]
}
```

Segunda cartilla vista: plantilla en blanco "Mi pequeño Sebastián", con los 12 tipos del catálogo, tres espacios para sticker de frasco por panel, fecha, próxima fecha y firma y sello. Sirve para probar que una cartilla vacía no genera registros.

Las fotos originales las tiene Gonzalo; pedírselas para el conjunto de pruebas.

## 19. Riesgos

| Riesgo | Impacto | Mitigación |
|---|---|---|
| La lectura comete errores en fechas | Alguien olvida una vacuna | Confirmación obligatoria, campos dudosos en durazno, nunca inventar dígitos, foto original siempre visible |
| Pérdida de datos al cambiar de teléfono (todo local) | Se pierde el historial | Respaldo manual sugerido tras escanear, respaldo del sistema, formato de respaldo estable |
| Costo del modelo de visión | Gasto sin control | Límite por instalación, tope mensual con alerta, imágenes reducidas |
| Avisos que no llegan (ahorro de batería, límite de iOS) | Pierde el valor principal | Reprogramar al abrir, aviso de seguimiento a los 3 días, prueba en teléfonos de gama baja |
| Diferencias de Compose Multiplatform en iOS (gestos, teclado, accesibilidad) | Experiencia rara en iPhone | Probar en iPhone real desde M0, usar componentes nativos para cámara, fechas y compartir |
| Catálogo por especie incorrecto | Confusión | "Ver todas" siempre disponible y validación veterinaria |

## 20. Preguntas que quedan (no bloquean el inicio)

| # | Pregunta | Valor por defecto mientras tanto |
|---|---|---|
| 1 | Revisión de marca de BestID en INDECOPI, dominio y redes | "BestID: cartilla de tu mascota". ID de la app: `com.nekodojo.bestid` `[Decidido]` (D45) |
| 2 | Confirmar Firebase AI Logic con Gemini y presupuesto mensual de lectura | Firebase AI Logic; decidir el modelo tras la prueba con 10 cartillas |
| 3 | A nombre de quién van las cuentas de Google Play, Apple y Firebase | Gonzalo |
| 4 | Herramienta de analítica | Sin analítica por ahora `[Decidido]` |
| 5 | Ilustraciones propias (avatar, estados vacíos, bienvenida) | Placeholders simples con los tokens |
| 6 | Disponibilidad semanal de Gonzalo para poner fechas a los hitos | Plan por hitos sin fechas |
| 7 | Lanzamiento solo en Perú o en más países | Solo Perú en ambas tiendas |

## 21. Referencias técnicas

- Compose Multiplatform estable en iOS desde 1.8.0: https://blog.jetbrains.com/kotlin/2025/05/compose-multiplatform-1-8-0-released-compose-multiplatform-for-ios-is-stable-and-production-ready/
- Room para Kotlin Multiplatform: https://developer.android.com/kotlin/multiplatform/room
- Firebase no tiene SDK oficial de KMP (pedido abierto): https://firebase.uservoice.com/forums/948424-general/suggestions/46591717-support-kotlin-multiplatform ; por eso Firebase AI Logic se usa con los SDK nativos detrás de `CardReader` y no en código común. Los datos de la app no pasan por Firebase.
- Firebase AI Logic, precios y obligación de App Check desde el 2 de noviembre de 2026: https://firebase.google.com/docs/ai-logic/pricing
- Librería KMP experimental de la comunidad para Firebase AI Logic: https://github.com/SeanChinJunKai/firebase-ai-kmp
- Sentry para Compose Multiplatform: https://docs.sentry.io/platforms/kotlin/guides/compose-multiplatform
- PostHog para KMP: https://posthog.com/docs/libraries/kmp
- Competencia mencionada en una conversación previa y no verificada: Pawza, MyPetID, Dexter, YourPetDiary, MyAnimalsApp, Boopable, MyPetHealth, Carnet Mascota, Chiwo, VetCard, Petcarnet.
