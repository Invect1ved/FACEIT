package com.example.faceit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceit.data.repository.PlayerRepository
import com.example.faceit.model.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PlayersListUiState {
    data object Loading : PlayersListUiState
    data object Empty : PlayersListUiState
    data class Success(val players: List<Player>) : PlayersListUiState
}

@HiltViewModel
class PlayersListViewModel @Inject constructor(
    private val repository: PlayerRepository
) : ViewModel() {

    val uiState: StateFlow<PlayersListUiState> = repository.observePlayers()
        .map { players ->
            if (players.isEmpty()) PlayersListUiState.Empty
            else PlayersListUiState.Success(players)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayersListUiState.Loading
        )

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            repository.deletePlayer(player)
        }
    }
}
