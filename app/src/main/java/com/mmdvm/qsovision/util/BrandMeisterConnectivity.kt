package com.mmdvm.qsovision.util

import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

object BrandMeisterConnectivity {
    data class Result(val hose: HostResult, val dashboard: HostResult) {
        val ok: Boolean get() = hose.ok && dashboard.ok
        fun message(): String = if (ok) "BrandMeister network ready" else {
            listOf(hose, dashboard).filterNot { it.ok }.joinToString(" • ") { it.message }
        }
    }

    data class HostResult(val host: String, val address: String?, val ok: Boolean, val message: String)

    fun check(): Result = Result(checkHost("hose.brandmeister.network"), checkHost("brandmeister.network"))

    private fun checkHost(host: String): HostResult {
        return try {
        val address = InetAddress.getAllByName(host).firstOrNull()?.hostAddress
        if (address == null) return HostResult(host, null, false, "$host DNS unavailable")
        val connection = (URL("https://$host/").openConnection() as HttpURLConnection).apply {
            connectTimeout = 7000
            readTimeout = 7000
            instanceFollowRedirects = true
            requestMethod = "HEAD"
        }
        val code = connection.responseCode
        connection.disconnect()
        if (code in 200..499) HostResult(host, address, true, "$host ready")
        else HostResult(host, address, false, "$host HTTPS error $code")
    } catch (error: Exception) {
        HostResult(host, null, false, "$host unavailable (${error.javaClass.simpleName})")
        }
    }
}
