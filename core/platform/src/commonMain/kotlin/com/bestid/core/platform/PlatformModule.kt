package com.bestid.core.platform

import org.koin.core.module.Module

/** Adaptadores de los puertos de plataforma (por ahora, `NotificationScheduler`). */
expect fun platformModule(): Module
