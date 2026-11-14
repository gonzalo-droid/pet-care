package com.bestid.core.platform

import android.content.Context
import android.content.pm.ApplicationInfo
import com.bestid.core.domain.port.AppInfo

class AndroidAppInfo(private val context: Context) : AppInfo {
    override val versionName: String
        get() = runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "?"

    override val isDebugBuild: Boolean
        get() = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
}
