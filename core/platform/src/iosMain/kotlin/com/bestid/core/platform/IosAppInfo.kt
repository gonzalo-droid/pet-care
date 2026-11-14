package com.bestid.core.platform

import com.bestid.core.domain.port.AppInfo
import platform.Foundation.NSBundle

class IosAppInfo : AppInfo {
    override val versionName: String
        get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "?"
}
