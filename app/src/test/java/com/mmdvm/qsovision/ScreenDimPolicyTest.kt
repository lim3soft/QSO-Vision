package com.mmdvm.qsovision

import com.mmdvm.qsovision.ui.ScreenDimPolicy
import org.junit.Assert.*
import org.junit.Test

class ScreenDimPolicyTest {
    @Test fun timedModesDimAtBoundaryAndTouchRestores() {
        for (seconds in listOf(30, 60, 120, 180, 300, 600, 900)) {
            var time = 0L
            val policy = ScreenDimPolicy { time }.apply { modeSeconds = seconds }
            time = seconds * 1000L - 1
            assertFalse(policy.dimmed(false))
            time++
            assertTrue(policy.dimmed(false))
            policy.touch()
            assertFalse(policy.dimmed(false))
        }
    }
    @Test fun smartDimsAfterSilenceAndSpeechRestores() {
        var time = 0L
        val policy = ScreenDimPolicy { time }.apply { modeSeconds = -1 }
        time = 9_999; assertFalse(policy.dimmed(false))
        time = 10_000; assertTrue(policy.dimmed(false))
        assertFalse(policy.dimmed(true))
        time = 19_999; assertFalse(policy.dimmed(false))
        time = 20_000; assertTrue(policy.dimmed(false))
        policy.touch(); assertFalse(policy.dimmed(false))
    }
    @Test fun alwaysOnNeverDimsAndResetRestartsTimeout() {
        var time = 0L
        val policy = ScreenDimPolicy { time }
        time = 9_000_000; assertFalse(policy.dimmed(false))
        policy.modeSeconds = 30; policy.reset()
        assertFalse(policy.dimmed(false))
        time += 30_000; assertTrue(policy.dimmed(false))
    }
    @Test fun timedModeDoesNotChangeWithSpeech() {
        var time = 0L
        val policy = ScreenDimPolicy { time }.apply { modeSeconds = 30 }
        time = 30_000; assertTrue(policy.dimmed(true))
    }
}
