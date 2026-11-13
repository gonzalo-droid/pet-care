package com.bestid.app

import android.app.Application
import com.bestid.app.di.initKoin
import org.koin.android.ext.koin.androidContext

class BestIdApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@BestIdApplication) }
    }
}
