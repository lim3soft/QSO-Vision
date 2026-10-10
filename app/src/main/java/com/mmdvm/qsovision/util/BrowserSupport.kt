package com.mmdvm.qsovision.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import android.webkit.WebView

/** System WebView does not require Google Play Services. Never install/replace
 * a provider or bypass its certificate checks on the user's behalf. */
object BrowserSupport {
    data class Result(val view: WebView?, val error: String?)

    fun create(context: Context): Result = try {
        val view = WebView(context)
        if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            // Debug tooling is optional; never discard a usable view if it fails.
            runCatching { WebView.setWebContentsDebuggingEnabled(true) }
        }
        Result(view, null)
    } catch (error: RuntimeException) {
        unavailable(error)
    } catch (error: LinkageError) {
        unavailable(error)
    }

    private fun unavailable(error: Throwable): Result {
        Log.e("QsoBrowser", "System browser could not initialize", error)
        return Result(null, "Embedded browser unavailable • enable or update your device's WebView provider. Google Play Services is not required. Tap connection status for help.")
    }
}
