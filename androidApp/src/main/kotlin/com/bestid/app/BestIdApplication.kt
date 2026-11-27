package com.bestid.app

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.bestid.app.di.initKoin
import org.koin.android.ext.koin.androidContext

class BestIdApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@BestIdApplication) }
        // D48: solo existe en un release compilado con -PcrashlyticsTestCrash=true (docs/CRASHLYTICS.md).
        if (resources.getBoolean(R.bool.crashlytics_test_crash)) {
            Handler(Looper.getMainLooper()).postDelayed({ throw RuntimeException("Crash de prueba de Crashlytics") }, 5_000)
        }
    }
}
