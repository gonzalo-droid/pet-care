# Pulido visual Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Aplicar el pulido visual aprobado (D44): sombra en vez de borde, durazno solo para lo dudoso, componentes `DateTile`, `CountdownRing`, `TimelineItem`, `ProgressRow`, `ReviewStrip`, y reordenar Inicio, Cartilla y el carnet.

**Architecture:** Los tokens y componentes nuevos viven en `core/designsystem` (sin depender de `core/model`). La lógica pura (progreso del anillo, agrupación por año, datos faltantes) va en `core/domain` con pruebas en `commonTest`. Las pantallas solo combinan componentes.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform 1.12.2, Material 3, kotlinx-datetime, Compose Resources.

**Spec:** `docs/superpowers/specs/2026-11-20-pulido-visual-design.md` (y la maqueta enlazada ahí).

## Global Constraints

- Rama `ux/pulido-visual`. Commits pequeños en español con prefijo `feat:`/`fix:`/`test:`/`docs:`, sin atribución de IA.
- Ningún color, `dp` ni `sp` suelto en `feature/*` (`.github/scripts/check-rules.sh` debe pasar).
- Textos nuevos en Compose Resources es-PE, con `plurals` para conteos.
- Accesibilidad: toques de 48 dp o más, texto de 13 sp o más, contraste de 4,5:1 o más, estados nunca solo por color, texto al 200 % sin cortes (D43: con `isLargeFont()` lo que va lado a lado se apila).
- No cambia paleta, fuente, navegación, modelo de datos, flujos ni el PDF.
- Las etiquetas existentes de los botones principales se mantienen ("Agregar vacuna" en Inicio y Cartilla).

## Review Focus

1. Próxima dosis atrasada o de hoy: el anillo queda vacío/lleno sin romperse y el texto dice "Atrasada N días" u "Hoy" (prueba en Task 1).
2. Registro con próxima fecha pero sin dosis anterior conocida (cuidado sin `lastDate`): `countdownProgress` devuelve `null` y el anillo se dibuja sin progreso (Task 1).
3. Cartilla con registros de un solo año y con años salteados (2025, 2022): un encabezado por año presente, en orden descendente (Task 1).
4. Mascota con todos los datos llenos: no aparece la fila de progreso; con ninguno, la grilla no queda vacía (Tutor ausente → solo la fila) (Task 1 y Task 5).
5. Letra al 200 %: el anillo y su texto se apilan y la fecha del `TimelineItem` va encima; la prueba de iOS `AccessibilityUiTest` ya falla antes de este trabajo por `bodyStrong`, así que se verifica en el emulador con capturas (Task 6).

---

### Task 1: Lógica pura en `core/domain`

**Files:**
- Create: `core/domain/src/commonMain/kotlin/com/bestid/core/domain/schedule/Countdown.kt`
- Create: `core/domain/src/commonMain/kotlin/com/bestid/core/domain/usecase/RecordGrouping.kt`
- Create: `core/domain/src/commonMain/kotlin/com/bestid/core/domain/usecase/PetCompleteness.kt`
- Test: `core/domain/src/commonTest/kotlin/com/bestid/core/domain/PolishLogicTest.kt`

**Interfaces:**
- Produces:
  - `fun countdownProgress(from: LocalDate?, to: LocalDate, today: LocalDate): Float?` — fracción transcurrida en `[0f, 1f]`; `null` si `from == null` o `from >= to`.
  - `fun List<VaccineRecord>.groupedByYear(): List<Pair<Int, List<VaccineRecord>>>` — años descendentes, registros por `applicationDate` descendente.
  - `enum class PetField { BREED, BIRTH_DATE, MARKS, OWNER }` y `fun Pet.missingFields(): List<PetField>` (en ese orden).

- [ ] **Step 1: Write the failing tests**

```kotlin
@Test fun progressMidway() = assertEquals(0.5f, countdownProgress(d(2026,1,1), d(2026,1,11), d(2026,1,6)))
@Test fun progressClampsOverdue() = assertEquals(1f, countdownProgress(d(2026,1,1), d(2026,1,11), d(2026,2,1)))
@Test fun progressBeforeStartIsZero() = assertEquals(0f, countdownProgress(d(2026,1,1), d(2026,1,11), d(2025,12,1)))
@Test fun progressNullWithoutFrom() = assertNull(countdownProgress(null, d(2026,1,11), d(2026,1,6)))
@Test fun progressNullWhenRangeEmpty() = assertNull(countdownProgress(d(2026,1,11), d(2026,1,11), d(2026,1,6)))
@Test fun groupsByYearDescending() // registros 2022-03-01, 2025-12-21, 2025-06-01 → [(2025,[12-21, 06-01]), (2022,[03-01])]
@Test fun missingFieldsInOrder() // pet sin breed, birthDate ni ownerName, con marks → [BREED, BIRTH_DATE, OWNER]
@Test fun noMissingFieldsWhenComplete() // → emptyList()
```

Usar las fábricas de `Fakes.kt` para `VaccineRecord` y `Pet`; un campo en blanco cuenta como faltante.

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :core:domain:jvmTest --tests "*PolishLogicTest*"`
Expected: FAIL (referencias no resueltas)

- [ ] **Step 3: Implement the three files** with the signatures above (`daysUntil` de kotlinx-datetime).

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :core:domain:jvmTest --tests "*PolishLogicTest*"`
Expected: PASS

- [ ] **Step 5: Commit** — `feat: progreso de la cuenta regresiva, cartilla por año y datos faltantes`

### Task 2: Token de elevación y tarjetas con sombra

**Files:**
- Modify: `core/designsystem/.../theme/Dimens.kt` (agregar `BestIdElevation`), `theme/AppTheme.kt` (proveerla y exponer `BestIdTheme.elevation`), `theme/Color.kt` (agregar `shadow` = `ink` con alfa 0.18f)
- Modify: `component/Cards.kt` (`BaseCard`), `component/Pet.kt` (`PetSelector`, `PetIdCard`), `component/Scaffolds.kt:215` (botón de ícono de la barra)

**Interfaces:**
- Produces: `data class BestIdElevation(val card: Dp = 6.dp)`; `BestIdTheme.elevation.card`; `BestIdColors.shadow: Color`.

- [ ] **Step 1:** En `BaseCard`, cuando `border != null` (superficies), reemplazar el `border` por `Modifier.shadow(elevation.card, shape, ambientColor = colors.shadow, spotColor = colors.shadow)` antes del `clip`; `PlainCard` deja de pasar `colors.line`. Igual en `PetSelector`, `PetIdCard` y el botón de ícono de `Scaffolds.kt`. Campos, `SecondaryButton`, `DashedButton` y chips no cambian.
- [ ] **Step 2:** Agregar en `ContrastTest` una aserción de que `muted` sobre `surface` sigue ≥ 4,5 (la tarjeta pierde borde pero no fondo).
- [ ] **Step 3: Verify** — `./gradlew :core:designsystem:jvmTest :androidApp:assembleDebug` PASS; `.github/scripts/check-rules.sh` sin errores.
- [ ] **Step 4: Commit** — `feat: tarjetas con sombra suave en lugar de borde`

### Task 3: Componentes nuevos del design system

**Files:**
- Create: `core/designsystem/.../component/Polish.kt`
- Modify: `core/designsystem/src/commonMain/composeResources/values/strings.xml`, `component/Records.kt` (`VaccineRecordCard` sin fecha propia cuando va en timeline), catálogo de componentes (donde se listan las previews de M0)

**Interfaces:**
- Produces (todas `@Composable`, `modifier: Modifier = Modifier` al final):
  - `DateTile(day: String, month: String, contentDescription: String, review: Boolean = false)` — 52 dp de ancho, día en `typography.subtitle` con `FontFeatureSettings "tnum"`, mes en `eyebrow`; `clearAndSetSemantics { contentDescription }`.
  - `CountdownRing(value: String, unit: String, progress: Float?)` — 112 dp, trazo 10 dp, `track` de fondo y `primary` de progreso (redondeado, desde arriba); `value` en `typography.hero`. Decorativo: `clearAndSetSemantics {}`; el que lo usa pone el texto accesible.
  - `ReviewStrip(text: String, actionText: String, onAction: () -> Unit)` — fondo `alert`, ícono `Warning`, alto mínimo `minTouch`, acción como pastilla `surface`/`alertInk` de 40 dp dentro de la fila tocable completa (`clickable` en la fila).
  - `TimelineItem(date: @Composable () -> Unit, content: @Composable () -> Unit)` — fecha a la izquierda con riel `track` de 2 dp; con `isLargeFont()`, fecha arriba y sin riel.
  - `ProgressRow(text: String, progress: Float, actionText: String, onClick: () -> Unit)` — fondo `bg`, `LinearProgressIndicator` con `primary`/`track`, toda la fila tocable, rol botón.
  - `VaccineRecordCard`: nuevo parámetro `date: String?` (si es `null`, no muestra la fila de fecha; el chevron va con los productos) y los chips de revisión con prefijo "!" vía `BadgeTone.ALERT`.
- Strings nuevos: ninguno en el design system; los textos (incluido el "!" de revisión y la unidad "días") llegan como parámetros desde cada feature.

- [ ] **Step 1:** Implementar `Polish.kt` con previews: normal, revisión, `fontScale = 2f`, progreso `null`, `0f`, `1f`.
- [ ] **Step 2:** Agregar los componentes al catálogo de componentes.
- [ ] **Step 3: Verify** — `./gradlew :core:designsystem:jvmTest :androidApp:assembleDebug` PASS; `check-rules.sh` limpio.
- [ ] **Step 4: Commit** — `feat: DateTile, CountdownRing, ReviewStrip, TimelineItem y ProgressRow`

### Task 4: Inicio

**Files:**
- Modify: `feature/home/.../HomeScreen.kt`, `HomeViewModel.kt`, `feature/home/src/commonMain/composeResources/values/strings.xml`

**Interfaces:**
- Consumes: `countdownProgress`, `CountdownRing`, `ReviewStrip`, `Badge`.
- Produces: `HomeState.toConfirm: Int` (registros con `nextDateConfirmed == false` o `typeIdsToConfirm` no vacío) y `Upcoming.from: LocalDate?` (vacuna: `record.applicationDate`; cuidado: `task.lastDate`).

- [ ] **Step 1:** Quitar el `InfoCard` de la mascota. Debajo del saludo, subtítulo `body`/`muted`: `home_summary` = "%1$s · %2$s" con plurales `home_vaccines_count` ("%d vacuna registrada"/"%d vacunas registradas") y `home_to_confirm_count` ("%d dato por confirmar"/"%d datos por confirmar"); la segunda parte solo si `toConfirm > 0`.
- [ ] **Step 2:** `NextDueHero` pasa a `PlainCard(onClick)`: `Eyebrow(home_next)`, fila (columna con letra grande) con `CountdownRing(value, unit, countdownProgress(due.from, due.date, today))` y columna con nombre (`card`), fecha larga (`body`/`muted`) y, si es vacuna no confirmada, `Badge("! " + home_year_to_confirm, ALERT)`. Para `daysLeft == 0` el anillo muestra "Hoy" sin unidad; atrasada: valor `-daysLeft`, unidad `home_days_overdue_unit` ("días tarde"), progreso 1f. Semántica de la tarjeta: `home_next_a11y` = "Próxima: %1$s, %2$s, en %3$s".
- [ ] **Step 3:** Si la vacuna tiene fecha no confirmada, `ReviewStrip(home_year_unreadable = "El año no se lee en la cartilla.", home_confirm = "Confirmar", onAction = abrir el registro)`.
- [ ] **Step 4:** `StatCard` gana `icon: DrawableResource? = null` (cuadro `primarySoft` de 34 dp arriba): `Vaccines` para última vacuna y el ícono de peso existente para Peso.
- [ ] **Step 5: Verify** — `./gradlew allTests :androidApp:assembleDebug` PASS; `check-rules.sh` limpio.
- [ ] **Step 6: Commit** — `feat: Inicio con cuenta regresiva y aviso de dato dudoso`

### Task 5: Cartilla y carnet

**Files:**
- Modify: `feature/cartilla/.../CartillaScreen.kt`, `CartillaViewModels.kt`, strings de cartilla
- Modify: `feature/pet/.../PetScreen.kt`, `core/designsystem/.../component/Pet.kt` (`PetIdCard`), strings de pet y designsystem

**Interfaces:**
- Consumes: `groupedByYear`, `missingFields`, `PetField`, `DateTile`, `TimelineItem`, `ProgressRow`.
- Produces: `PetIdCard(name, photo, species, subtitle: String, details: List<Pair<String,String>>, vaccineCount: String, nextDate: String, completion: (@Composable () -> Unit)? = null, modifier)`.

- [ ] **Step 1 (Cartilla):** Subtítulo igual que Inicio (reusar el texto con plurales copiados a cartilla). `NextDueCard` → `PlainCard(onClick, showChevron = true)` con `DateTile(review = !due.confirmed)`, `Eyebrow`, nombre y `Badge("! Confirma el año", ALERT)` solo si no está confirmada; el texto "en N días/Hoy/Atrasada" se conserva debajo del nombre.
- [ ] **Step 2 (Cartilla):** Historial con `state.records.groupedByYear()`: por año un encabezado `SectionTitle(year)`, y cada registro como `TimelineItem(date = { DateTile(day, mesCorto, fechaLarga) }) { VaccineRecordCard(date = null, …) }`. Mes corto con un helper nuevo `LocalDate.formatMonthShort()` en `core/ui/DateFormat.kt` ("DIC"). Mantener "Agregar vacuna" y "Compartir".
- [ ] **Step 3 (Carnet):** `PetIdCard`: encabezado `primary` de 96 dp con trama de puntos dibujada con `drawBehind` (círculos `onPrimary` alfa 0.08 cada 44 dp), marca a la izquierda y `ds_id_card_title` "CARNET DE MASCOTA" a la derecha; avatar `LARGE` desplazado −44 dp con aro `surface` de 5 dp; nombre (`title`, heading) y `subtitle` ("Gata · Hembra"); `details` en grilla de 2 columnas (1 con letra grande); `completion`; separador punteado `line` con muescas `bg` en los extremos; pie con íconos `CheckCircle` + vacunas y `Calendar` + próxima.
- [ ] **Step 4 (Carnet):** En `PetScreen`, `details` sin especie ni sexo (van en `subtitle`); `completion` = `ProgressRow(pet_missing = "Faltan %1$s", progress = (4 - faltantes) / 4f, "Completar", onEditPet)` solo si `missingFields()` no está vacío, con la lista unida "raza, nacimiento y señas" (helper de unión con "y" en core/ui). Quitar `SecondaryButton` "Completar datos"; `DashedButton` → `TextAction(pet_add_another, icon = Add)`.
- [ ] **Step 5: Verify** — `./gradlew allTests :androidApp:assembleDebug` PASS; `check-rules.sh` limpio.
- [ ] **Step 6: Commit** — `feat: cartilla por año con fechas en calendario y carnet renovado`

### Task 6: Verificación, pruebas de UI y documentación

**Files:**
- Modify: `composeApp/src/iosTest/kotlin/com/bestid/app/AppFlowUiTest.kt` (textos que cambiaron: "Completar datos" ya no es botón)
- Modify: `docs/SYSTEM_DESIGN.md` sección 7 (tabla de componentes: agregar los cinco nuevos; nota de elevación) y `docs/ux-audit-2026-11-19.md` no se toca.

- [ ] **Step 1:** Ajustar `AppFlowUiTest` a los textos nuevos; correr `./gradlew iosSimulatorArm64Test --tests "*AppFlowUiTest*"` → PASS.
- [ ] **Step 2:** Emulador: capturas de Inicio, Cartilla y Mascota a 100 % y 200 % (con fecha dudosa y sin ella, con carnet completo e incompleto). Revisar cortes, toques ≥ 48 dp y que ningún estado dependa solo del color. Devolver la escala de fuente a 1.0.
- [ ] **Step 3:** Auditoría rápida (`ux-audit`, modo Rápido) sobre las tres pantallas; arreglar lo crítico y alto.
- [ ] **Step 4:** Actualizar la sección 7 de `SYSTEM_DESIGN.md`.
- [ ] **Step 5: Commit** — `test: flujo de iOS con el pulido visual` y `docs: sección 7 con elevación y componentes nuevos`
