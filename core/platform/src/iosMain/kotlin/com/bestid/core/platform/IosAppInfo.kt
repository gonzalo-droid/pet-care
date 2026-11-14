package com.bestid.core.platform

import com.bestid.core.domain.port.AppInfo
import platform.Foundation.NSBundle
import kotlin.experimental.ExperimentalNativeApi

class IosAppInfo : AppInfo {
    override val versionName: String
        get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "?"

    @OptIn(ExperimentalNativeApi::class)
    override val isDebugBuild: Boolean
        get() = Platform.isDebugBinary
}
