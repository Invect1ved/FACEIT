package com.example.faceit.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.faceit.data.repository.PlayerRepository
import com.example.faceit.model.Match
import com.example.faceit.model.MatchResult
import com.example.faceit.model.Player
import com.example.faceit.ui.navigation.AddMatchRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddMatchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PlayerRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<AddMatchRoute>()
    private val playerId: Long = route.playerId
    private val matchId: Long = route.matchId

    val isEditMode: Boolean = matchId != 0L

    private val _player = MutableStateFlow<Player?>(null)
    val player: StateFlow<Player?> = _player.asStateFlow()

    private val _mapName = MutableStateFlow("")
    val mapName: StateFlow<String> = _mapName.asStateFlow()

    private val _result = MutableStateFlow(MatchResult.WIN)
    val result: StateFlow<MatchResult> = _result.asStateFlow()

    private val _kills = MutableStateFlow("")
    val kills: StateFlow<String> = _kills.asStateFlow()

    private val _deaths = MutableStateFlow("")
    val deaths: StateFlow<String> = _deaths.asStateFlow()

    private val _dateMillis = MutableStateFlow(System.currentTimeMillis())
    val dateMillis: StateFlow<Long> = _dateMillis.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observePlayer(playerId).collect { p ->
                _player.value = p
            }
        }
        if (isEditMode) {
            viewModelScope.launch {
                val m = repository.getMatchById(matchId)
                if (m != null && m.playerId == playerId) {
                    _mapName.value = m.mapName
                    _result.value = m.result
                    _kills.value = m.kills.toString()
                    _deaths.value = m.deaths.toString()
                    _dateMillis.value = m.date
                }
            }
        }
    }

    fun setMapName(value: String) {
        _mapName.update { value }
    }

    fun setResult(value: MatchResult) {
        _result.value = value
    }

    fun setKills(value: String) {
        _kills.update { value.filter { ch -> ch.isDigit() } }
    }

    fun setDeaths(value: String) {
        _deaths.update { value.filter { ch -> ch.isDigit() } }
    }

    fun setDateMillis(value: Long) {
        _dateMillis.value = value
    }

    fun saveMatch(onDone: () -> Unit) {
        viewModelScope.launch {
            val k = _kills.value.toIntOrNull() ?: 0
            val d = _deaths.value.toIntOrNull() ?: 0
            val entity = Match(
                id = if (isEditMode) matchId else 0L,
                playerId = playerId,
                mapName = _mapName.value.trim().ifEmpty { "—" },
                result = _result.value,
                kills = k,
                deaths = d,
                date = _dateMillis.value
            )
            if (isEditMode) {
                repository.updateMatch(entity)
            } else {
                repository.insertMatch(entity)
            }
            onDone()
        }
    }
}
