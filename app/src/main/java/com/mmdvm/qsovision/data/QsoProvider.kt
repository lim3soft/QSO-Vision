package com.mmdvm.qsovision.data
import com.mmdvm.qsovision.domain.model.*
import kotlinx.coroutines.flow.Flow
interface QsoProvider { fun events(talkgroup:Int):Flow<QsoEvent>; suspend fun connect(); suspend fun disconnect() }
