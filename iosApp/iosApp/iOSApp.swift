import SwiftUI
import ComposeApp
import FirebaseCore
import FirebaseCrashlytics

@main
struct iOSApp: App {
    init() {
        // D48 (CRA-04): Crashlytics solo en Release y solo si el build trae GoogleService-Info.plist
        // (no se versiona). Debug no recolecta.
        #if !DEBUG
        if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
            FirebaseApp.configure()
        }
        #endif
        #if CRASHLYTICS_TEST_CRASH
        // Solo existe si se compila con este flag a mano (docs/CRASHLYTICS.md).
        DispatchQueue.main.asyncAfter(deadline: .now() + 5) { fatalError("Crash de prueba de Crashlytics") }
        #endif
        AppStartKt.startApp()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
