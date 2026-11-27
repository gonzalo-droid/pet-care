# Pendientes de BestID

Tareas abiertas, por prioridad. Los IDs (`TAB-02`, `VER-01`…) son de la base KMP; el detalle y la evidencia están en
[auditorias/2026-11-26-base-kmp.md](auditorias/2026-11-26-base-kmp.md). Al cerrar una tarea, bórrala de aquí y
anota la decisión en `DECISIONS.md` si hubo una.

Release abierta: `release/0.6.0` (D50). No se cierra hasta que estén las de **Alta**.

## Alta (antes de cerrar 0.6.0)

- [ ] **Firebase (D48):** registrar las apps Android e iOS `com.nekodojo.bestid` en `bestid-c931a`, descargar
  `google-services.json` a `androidApp/` y `GoogleService-Info.plist` a `iosApp/iosApp/`, habilitar Crashlytics
  y probar el crash de prueba (`docs/CRASHLYTICS.md`).
- [ ] **PUB-15 / NEK-01 / NEK-02 — Política, términos y descarga en neko-dojo.** Tras la asesoría legal (sección 14):
  ficha en `neko-dojo/src/content/apps.ts` y páginas `privacy`, `terms`, `support` y la ruta común
  `/apps/[slug]/descargar`. Ajustes ya abre esas URLs (`AppLinks.kt`): deben responder 200 (PUB-06).
- [ ] **Correo de contacto:** confirmar `contacto@gondroid.dev` en `AppLinks.kt` o cambiarlo por uno de Nekodojo.
- [ ] **TAB-02 — Prueba en iPad** (letra grande, horizontal, Stage Manager) y **DOC-04 — capturas de tienda**,
  también las de iPad 13".
- [ ] **SEC-04 — Respaldo de llaves:** completar la tabla de `docs/BETA.md` (sección 6).
- [ ] **M3 — Elegir la tecnología de lectura de la cartilla** (D4, D29, sección 10) y hacer la prueba con 10
  cartillas reales.

## Media

- [ ] **COD-07 — Commits con atribución de IA.** 63 commits (13 y 14 nov.) llevan `Co-Authored-By: Claude`.
  Decidir si se reescribe `main` (repo público, force-push) o se deja anotado.
- [ ] **AJU-01 — Licencias** en Ajustes (las de `docs/licenses/`).
- [ ] **AJU-05 en iOS:** cuando haya ficha pública, abrir `itms-apps://…/id<ID>?action=write-review` en lugar
  del aviso nativo (`ExternalActions.ios.kt`).
- [ ] **Pruebas en dispositivo (una sola sesión):**
  - Release de Android e iOS en claro, letra grande y modo avión (TST-09, PUB-02).
  - TalkBack y VoiceOver por el flujo principal (A11Y-04).
  - Negar cámara, galería y avisos y seguir usando la app (PERM-02).
  - Tablet de 600 dp o más en horizontal (TIE-04).
  - Instalar el build nuevo encima del anterior con datos (PUB-04): en Android, que los avisos lleguen por los
    canales nuevos y desaparezca el canal viejo.
  - Hojas inferiores con teclado en iPhone (TRP-02).
  - Navegación por gestos y con 3 botones (TIE-03).
  - Que un aviso de prueba llegue en 1 minuto en Android.
- [ ] **Consolas de las tiendas:** Play App Signing (SEC-03), Data safety (PUB-10) y privacidad de App Store
  (PUB-12) según `docs/store/` (ahora declaran los informes de Crashlytics).
- [ ] **TestFlight:** probar el build 3 en el iPhone (pestañas, onboarding, fechas, demora al escribir) e invitar a
  Joseth y Miguel.

## Baja

- [ ] **TAB-01 — Listas en dos columnas** desde ~560 dp, si en iPad se ven vacías.
- [ ] **ARQ-05 — Scope de app calificado en Koin.**
- [ ] **TST-08 / CI-01 — Flujo E2E que cubra Android** y hook `pre-push`.
- [ ] **TRP-01 / CRA-02 — Activar R8:** las reglas ya están en `androidApp/proguard-rules.pro`; probar el release
  entero antes de subirlo.
- [ ] **UIS-01 — Esqueleto de carga** si alguna pantalla tarda.
- [ ] **Revisar animaciones** del detalle de vacuna, Ajustes y hojas inferiores.
- [ ] **"Probar aviso"** muestra un mensaje en pantalla al dispararse.
