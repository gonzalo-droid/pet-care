package com.bestid.app.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/**
 * Módulos de Koin de la app. Cada capa aporta el suyo a partir de M1
 * (datos, casos de uso, adaptadores de plataforma, ViewModels de cada feature).
 */
val appModules: List<Module> = emptyList()

/** Punto único de arranque de Koin. Android lo llama desde Application e iOS desde iOSApp.swift. */
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModules)
    }
}
