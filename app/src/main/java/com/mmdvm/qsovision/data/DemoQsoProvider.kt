package com.mmdvm.qsovision.data
import com.mmdvm.qsovision.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.util.UUID

class DemoQsoProvider : QsoProvider {
    override fun events(talkgroup: Int) = flow {
        var n = 0
        while (true) {
            delay(5000)
            val event = QsoEvent("demo-${UUID.randomUUID()}", listOf("9M2ABC", "W1AW", "G4XYZ")[n++ % 3], talkgroup, System.currentTimeMillis())
            emit(event)
            delay(6500)
            emit(event.copy(endedAt = System.currentTimeMillis()))
        }
    }
    override suspend fun connect() {}
    override suspend fun disconnect() {}
}
