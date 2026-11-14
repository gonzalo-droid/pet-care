package com.bestid.core.platform

import android.content.Context
import com.bestid.core.domain.port.AppInfo

class AndroidAppInfo(private val context: Context) : AppInfo {
    override val versionName: String
        get() = runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "?"
}
