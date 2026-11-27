# Crashlytics (D48)

Solo crashes fatales, solo en builds de release. Sin los archivos de Firebase la app compila igual y no
reporta nada (Gradle y Xcode lo avisan con un warning).

## Puesta en marcha (una vez)
1. Consola de Firebase → proyecto `bestid-c931a` → registrar las apps:
   - Android `com.nekodojo.bestid` → descargar `google-services.json` a `androidApp/`.
   - iOS `com.nekodojo.bestid` → descargar `GoogleService-Info.plist` a `iosApp/iosApp/`.
   Los dos están en `.gitignore` y el CI revisa que no se suban.
2. Consola → Crashlytics → "Habilitar".

Con eso:
- Android: `assembleRelease`/`bundleRelease` aplican los plugins (y suben el mapping de R8 cuando se active).
- iOS: la fase "Copy GoogleService-Info.plist" lo mete en la app y "Upload dSYM to Crashlytics" sube los
  dSYM en cada build Release (archive incluido). En Debug no se llama a `FirebaseApp.configure()`.

## Crash de prueba
Nada en la app lo dispara: el código solo se compila si se pide al compilar. Crashlytics no reporta con el
depurador conectado, así que abre la app desde el ícono, no desde Android Studio ni Xcode.

Android (release; sin la keystore de subida queda sin firmar, así que para instalarlo hace falta la llave):
```bash
./gradlew :androidApp:installRelease -PcrashlyticsTestCrash=true
```
Abre la app: a los 5 s se cierra. Ábrela otra vez para que envíe el reporte. Luego reinstala sin la
propiedad (`./gradlew :androidApp:installRelease`).

iOS (simulador, Release):
```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Release -destination 'id=<sim>' -derivedDataPath iosApp/build OTHER_SWIFT_FLAGS='$(inherited) -DCRASHLYTICS_TEST_CRASH' build
```
Instala `iosApp/build/Build/Products/Release-iphonesimulator/BestID.app` con `xcrun simctl install`, ábrela
desde el ícono, espera el cierre (5 s) y ábrela otra vez. El reporte tarda unos minutos en verse.

Nunca subas a tienda un build hecho con `-PcrashlyticsTestCrash` o `-DCRASHLYTICS_TEST_CRASH`.
