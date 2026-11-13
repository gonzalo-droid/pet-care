package com.bestid.app.di

import com.bestid.app.navigation.MainViewModel
import com.bestid.core.data.di.dataModule
import com.bestid.core.domain.di.domainModule
import com.bestid.core.platform.platformModule
import com.bestid.feature.cartilla.cartillaModule
import com.bestid.feature.health.healthModule
import com.bestid.feature.home.homeModule
import com.bestid.feature.onboarding.onboardingModule
import com.bestid.feature.pet.petModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private val appModule = module {
    viewModelOf(::MainViewModel)
}

/** Módulos de la app: datos y plataforma (adaptadores), dominio (casos de uso) y un módulo por feature. */
val appModules: List<Module> = listOf(
    dataModule,
    platformModule(),
    domainModule,
    appModule,
    homeModule,
    cartillaModule,
    healthModule,
    petModule,
    onboardingModule,
)

/** Punto único de arranque de Koin. Android lo llama desde Application e iOS desde iOSApp.swift. */
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModules)
    }
}
