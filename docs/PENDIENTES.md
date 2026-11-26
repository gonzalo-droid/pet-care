# Pendientes de BestID

Tareas abiertas, por prioridad. Los IDs (`TAB-02`, `VER-01`…) son de la base KMP; el detalle y la evidencia están en
[auditorias/2026-11-26-base-kmp.md](auditorias/2026-11-26-base-kmp.md). Al cerrar una tarea, bórrala de aquí y
anota la decisión en `DECISIONS.md` si hubo una.

## Alta

- [ ] **TAB-02 / GIT-07 — Decidir iPad.** Hoy está declarado (`TARGETED_DEVICE_FAMILY = "1,2"`) sin decisión. Antes
  de enviar a revisión de App Store: dejarlo en `"1"`, o hacerlo en su propia release con TAB-01, prueba en iPad y
  capturas de 13".
- [ ] **PUB-15 / NEK-01 — Política de privacidad y términos publicados.** Tras la asesoría legal (sección 14):
  ficha en `neko-dojo/src/content/apps.ts` y páginas `privacy`, `terms` y `support`.
- [ ] **AJU-01 / AJU-02 / PUB-06 — Ajustes completos.** Enlaces a política y términos, contacto con versión y
  modelo prellenados, y licencias.
- [ ] **VER-01 / VER-02 — Una sola fuente de versión.** `app-versionName` y `app-versionCode` en el catálogo,
  `checkIosVersion` en `check`, y sin `versionCode = 1` silencioso.
- [ ] **DOC-04 / SEC-05 — Capturas de tienda.** Generarlas y sumar sus carpetas de salida al `.gitignore`.
- [ ] **M3 — Elegir la tecnología de lectura de la cartilla** (D4, D29, sección 10) y hacer la prueba con 10
  cartillas reales.

## Media

- [ ] **COD-07 — Commits con atribución de IA.** 63 commits (13 y 14 nov.) llevan `Co-Authored-By: Claude`.
  Decidir si se reescribe `main` (repo público, force-push) o se deja anotado.
- [ ] **OBS-03 — Trazas de crash en iOS.** `setUnhandledExceptionHook` con `NSLog` en `AppStart.kt`.
- [ ] **TST-05 — Tests de ViewModel** con Turbine: VaccineForm, Home y Backup.
- [ ] **PUSH-03 — Un canal de Android por tipo de aviso** (vacunas y cuidados).
- [ ] **DOC-02 — `CHANGELOG.md`** desde 0.5.0.
- [ ] **DOC-01 — Alinear `CLAUDE.md` con D12:** Sentry no está en el stack todavía.
- [ ] **SEC-01 / SEC-02 / SEC-04 — Secretos.** `local.properties.example`, `TEAM_ID` en un `Secrets.xcconfig`
  ignorado, warning cuando falta la keystore y anotar dónde está el respaldo de llaves.
- [ ] **GIT-01..03 — Flujo de ramas.** Adoptar `release/X.Y.Z` y tags antes de la primera versión en tienda, o
  anotar la excepción en `DECISIONS.md`.
- [ ] **TRP-01 — Antes de activar R8:** regla keep de DataStore y probar el release.
- [ ] **Pruebas en dispositivo (una sola sesión):**
  - Release de Android e iOS en claro, letra grande y modo avión (TST-09, PUB-02).
  - TalkBack y VoiceOver por el flujo principal (A11Y-04).
  - Negar cámara, galería y avisos y seguir usando la app (PERM-02).
  - Tablet de 600 dp o más en horizontal (TIE-04).
  - Instalar el build nuevo encima del anterior con datos (PUB-04).
  - Hojas inferiores con teclado en iPhone (TRP-02).
  - Navegación por gestos y con 3 botones (TIE-03).
  - Que un aviso de prueba llegue en 1 minuto en Android.
- [ ] **Consolas de las tiendas:** Play App Signing (SEC-03), Data safety (PUB-10) y privacidad de App Store
  (PUB-12).
- [ ] **TestFlight:** probar el build 3 en el iPhone (pestañas, onboarding, fechas, demora al escribir) e invitar a
  Joseth y Miguel.

## Baja

- [ ] **TAB-01 — Ancho máximo de contenido** (~720 dp), también por TIE-04.
- [ ] **COD-05 — Partir pantallas largas:** `VaccineFormScreen.kt` (366) y `HealthScreen.kt` (309).
- [ ] **ARQ-05 — Scope de app calificado en Koin.**
- [ ] **TST-08 / CI-01 — Flujo E2E que cubra Android** y hook `pre-push`.
- [ ] **PERF-01 — Medir tamaño de AAB/IPA y arranque** en cada release.
- [ ] **AJU-03 / AJU-04 / AJU-05 / NEK-02 — Compartir y valorar la app**, cuando haya fichas en las tiendas.
- [ ] **UIS-01 — Esqueleto de carga** si alguna pantalla tarda.
- [ ] **DOC-03 — README** con capturas reales y versión.
- [ ] **Revisar animaciones** del detalle de vacuna, Ajustes y hojas inferiores.
- [ ] **"Probar aviso"** muestra un mensaje en pantalla al dispararse.

## Antes de activar Sentry (D12)

- [ ] **CRA-01** — SDK y DSN condicionales, con warning si falta.
- [ ] **CRA-02** — Subir el mapping y los dSYM.
- [ ] **CRA-04** — Debug no recolecta; crash de prueba solo con flag.
- [ ] **CRA-05 / OBS-01** — Solo fatales, o una interfaz `CrashReporter` común.
- [ ] **CRA-06** — Actualizar la política, Data safety y la privacidad de App Store.
