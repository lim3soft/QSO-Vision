package com.mmdvm.qsovision.ui

/** Time values use elapsedRealtime, never the adjustable wall clock. */
class ScreenDimPolicy(private val now: () -> Long) {
    var modeSeconds: Int = 0 // 0 always on; -1 smart; positive inactivity seconds
    private var lastTouch = now()
    private var lastVoice = now()
    fun touch() { lastTouch = now() }
    fun reset() { lastTouch = now(); lastVoice = now() }
    fun dimmed(talking: Boolean): Boolean {
        if (talking) lastVoice = now()
        return when {
            modeSeconds == 0 -> false
            modeSeconds == -1 -> !talking && now() - maxOf(lastVoice, lastTouch) >= 10_000
            else -> now() - lastTouch >= modeSeconds * 1000L
        }
    }
}
