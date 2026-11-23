# Beta cerrada (M5)

Objetivo (sección 17): **una semana de beta con 10 dueños, sin errores bloqueantes.** Android por la prueba interna de Google Play e iOS por TestFlight.

## 1. Antes de subir: revisión local

En tu Mac, con la rama al día:

| Comprobación | Comando o acción | Resultado esperado |
|---|---|---|
| Reglas del proyecto | `.github/scripts/check-rules.sh` | Todo ✓ |
| Android y pruebas | `./gradlew :androidApp:assembleDebug allTests` | BUILD SUCCESSFUL |
| iOS: pruebas, flujo M1 a M4 y accesibilidad al 200 % | `./gradlew iosSimulatorArm64Test` | BUILD SUCCESSFUL. Capturas en `composeApp/build/ui-screenshots/` |
| Procesado de fotos en Android (con emulador abierto) | `./gradlew :core:platform:connectedAndroidDeviceTest` | Pruebas en verde |
| Avisos en Android de punta a punta (emulador) | `.github/scripts/android-reminders-e2e.sh build/android-e2e` | "OK: llegó el aviso…" dos veces |
| Teléfonos reales | Ver la lista de la sección 4 | Todo ✓ |

## 2. Android: prueba interna de Google Play

1. **Llave de subida** (una sola vez; guárdala fuera del repositorio y con respaldo):
   ```
   keytool -genkeypair -keystore ~/bestid-upload.jks -alias bestid -keyalg RSA -keysize 2048 -validity 10000
   ```
2. En `local.properties` (no se sube al repositorio):
   ```
   BESTID_UPLOAD_STORE_FILE=/Users/<tú>/bestid-upload.jks
   BESTID_UPLOAD_STORE_PASSWORD=...
   BESTID_UPLOAD_KEY_ALIAS=bestid
   BESTID_UPLOAD_KEY_PASSWORD=...
   ```
3. Generar el paquete, subiendo `versionCode` en cada subida:
   ```
   ./gradlew :androidApp:bundleRelease -Pbestid.versionCode=1
   ```
   Queda en `androidApp/build/outputs/bundle/release/androidApp-release.aab`.
4. En Play Console:
   1. Crear la app `com.nekodojo.bestid` con la ficha de `docs/store/google-play.md`.
   2. Activar la firma de apps de Google Play (Google guarda la llave final y tú la de subida).
   3. Completar "Seguridad de los datos" y "Contenido de la app".
   4. En **Pruebas → Prueba interna**: subir el `.aab`, crear la lista de testers (correos de Gmail) y compartir el enlace de participación.

## 3. iOS: TestFlight

1. En `iosApp/Configuration/Config.xcconfig`:
   - completar `TEAM_ID`;
   - subir `CURRENT_PROJECT_VERSION` en cada subida;
   - `MARKETING_VERSION` es la versión visible.
2. En App Store Connect, crear la app con el bundle `com.nekodojo.bestid` y la ficha de `docs/store/app-store.md`.
3. En Xcode:
   1. Elegir **Any iOS Device (arm64)**.
   2. Ir a **Product → Archive** y luego **Distribute App → App Store Connect → Upload**. La primera compilación de Kotlin/Native tarda varios minutos.
4. En App Store Connect → TestFlight:
   1. Crear un grupo de **prueba interna** (hasta 100 personas de tu equipo, sin revisión de Apple) o **externa** (necesita una revisión corta de Apple).
   2. Agregar los correos e invitar.

## 4. Lista para probar en teléfonos reales (antes de invitar)

En un Android y un iPhone, sin internet salvo donde se indica:

- [ ] Primer uso: Bienvenida → "Empezar con su cartilla" → Lilith con foto → 2 páginas de la cartilla → sus 2 vacunas a mano mirando la foto (M3).
- [ ] Las fotos quedan en el detalle de cada vacuna; la foto de Lilith, en su carnet.
- [ ] Avisos: con la app cerrada y tras reiniciar el teléfono (M2). En debug, Ajustes → "Probar aviso en 1 minuto"; en la beta, una vacuna con próxima fecha cercana.
- [ ] Cambiar la hora de los avisos en Ajustes.
- [ ] Compartir la ficha por WhatsApp (con internet): llega el PDF, sin la dirección (M4).
- [ ] Exportar el respaldo en Android, importarlo en el iPhone, y al revés (M4).
- [ ] Texto del sistema al máximo: nada se corta.
- [ ] TalkBack y VoiceOver: se puede registrar una vacuna solo con el lector de pantalla.
- [ ] Negar el permiso de cámara (iOS) y el de avisos: aparece la tarjeta con "Abrir ajustes".

## 5. La semana de beta

**Invitación a los 10 dueños** (ejemplo para WhatsApp):

> ¡Hola! Estoy probando BestID, una app para llevar la cartilla de vacunas de tu mascota y recibir avisos. ¿Me ayudas una semana? Solo tienes que registrar a tu mascota con su cartilla y usarla normalmente. Te paso el enlace para instalarla. Si algo falla o no se entiende, mándame una captura por aquí.

**Qué pedirles:**
1. Registrar al menos una mascota con todas las vacunas de su cartilla, con fotos.
2. Esperar un aviso (o poner una próxima fecha en 1 o 2 días).
3. Compartir la ficha con alguien.
4. Al final: una pregunta corta. ¿Qué fue lo más difícil? ¿La volverías a usar?

**Registro de problemas:** un issue en GitHub por problema, con:
- etiqueta `beta`;
- gravedad:
  - **bloqueante:** se cierra la app, se pierden datos, no llega un aviso o no se puede registrar una vacuna;
  - **importante:** algo confunde o hay que intentarlo varias veces;
  - **menor:** texto o diseño;
- teléfono y versión del sistema;
- pasos para que vuelva a pasar.

**Terminado cuando:** 7 días seguidos sin problemas bloqueantes abiertos.

**Métricas:** la sección 16 decidió no usar analítica (D5), así que no hay métricas automáticas. Durante la beta se miden a mano con la pregunta final:
- cuántos activaron los avisos;
- cuántos siguieron usándola al final de la semana.
