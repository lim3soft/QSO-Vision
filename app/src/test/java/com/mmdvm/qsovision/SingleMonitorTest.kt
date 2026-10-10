package com.mmdvm.qsovision

import com.mmdvm.qsovision.ui.QsoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SingleMonitorTest {
    @Test fun switchingCancelsOldEventsAndFiltersHistory() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val model = QsoViewModel()
            model.setDemo(true)
            advanceTimeBy(5001); runCurrent()
            assertEquals(46020, model.active.value?.talkgroup)
            model.select(91)
            assertNull(model.active.value)
            advanceTimeBy(11501); runCurrent()
            assertNull(model.active.value)
            assertEquals(1, model.history.value.size)
            assertTrue(model.history.value.all { it.talkgroup == 91 && it.endedAt != null })
            model.select(46020); runCurrent()
            assertTrue(model.history.value.isEmpty())
            model.setDemo(false); runCurrent()
        } finally { Dispatchers.resetMain() }
    }
    @Test fun defaultModeNeverFabricatesLiveActivity() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val model = QsoViewModel()
            model.select(91)
            advanceTimeBy(60000); runCurrent()
            assertFalse(model.demo.value)
            assertNull(model.active.value)
            assertTrue(model.history.value.isEmpty())
        } finally { Dispatchers.resetMain() }
    }
    @Test fun endingDemoClearsOnAirAndDisplayedHistory() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val model = QsoViewModel()
            model.setDemo(true)
            advanceTimeBy(11501); runCurrent()
            assertEquals(1, model.history.value.size)
            model.setDemo(false); runCurrent()
            assertNull(model.active.value)
            assertTrue(model.history.value.isEmpty())
        } finally { Dispatchers.resetMain() }
    }
}
