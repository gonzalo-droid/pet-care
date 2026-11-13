package com.bestid.core.platform

import com.bestid.core.domain.port.ImageProcessor
import com.bestid.core.domain.port.NotificationScheduler
import com.bestid.core.platform.image.AndroidImageProcessor
import com.bestid.core.platform.notification.AndroidNotificationScheduler
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<NotificationScheduler> { AndroidNotificationScheduler(get()) }
    single<ImageProcessor> { AndroidImageProcessor() }
}
