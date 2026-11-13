package com.bestid.core.platform

import com.bestid.core.domain.port.ImageProcessor
import com.bestid.core.domain.port.NotificationScheduler
import com.bestid.core.platform.image.IosImageProcessor
import com.bestid.core.platform.notification.IosNotificationScheduler
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<NotificationScheduler> { IosNotificationScheduler() }
    single<ImageProcessor> { IosImageProcessor() }
}
