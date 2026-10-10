package com.mmdvm.qsovision.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmdvm.qsovision.country.CountryResolver
import com.mmdvm.qsovision.data.DemoQsoProvider
import com.mmdvm.qsovision.domain.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QsoViewModel : ViewModel() {
    private val resolver = CountryResolver()
    private var subscription: Job? = null
    val selected = MutableStateFlow(46020)
    val demo = MutableStateFlow(false)
    val active = MutableStateFlow<QsoEvent?>(null)
    private val allHistory = MutableStateFlow<List<QsoEvent>>(emptyList())
    val history = combine(allHistory, selected, demo) { events, tg, enabled ->
        if (enabled) events.filter { it.talkgroup == tg } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun select(id: Int) {
        require(id in 1..16777215)
        subscription?.cancel()
        selected.value = id
        active.value = null
        if (demo.value) subscription = viewModelScope.launch {
            DemoQsoProvider().events(id).collect { event ->
                if (event.talkgroup != selected.value) return@collect
                val resolved = event.copy(country = resolver.resolve(event.callsign))
                if (event.endedAt == null) active.value = resolved
                else {
                    if (active.value?.id == event.id) active.value = null
                    allHistory.update { (listOf(resolved) + it.filterNot { old -> old.id == resolved.id }).take(500) }
                }
            }
        }
    }

    fun setDemo(enabled: Boolean) {
        demo.value = enabled
        select(selected.value)
    }
}
