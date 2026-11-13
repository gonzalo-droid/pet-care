import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        AppStartKt.startApp()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
