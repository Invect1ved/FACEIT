package com.example.faceit.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.faceit.data.repository.PlayerRepository
import com.example.faceit.model.Match
import com.example.faceit.model.Player
import com.example.faceit.ui.navigation.PlayerDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PlayerDetailUiState {
    data object Loading : PlayerDetailUiState
    data object NotFound : PlayerDetailUiState
    data class Success(val player: Player, val matches: List<Match>) : PlayerDetailUiState
}

@HiltViewModel
class PlayerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PlayerRepository
) : ViewModel() {

    private val playerId: Long = savedStateHandle.toRoute<PlayerDetailRoute>().playerId

    val uiState: StateFlow<PlayerDetailUiState> = combine(
        repository.observePlayer(playerId),
        repository.observeMatches(playerId)
    ) { player, matches ->
        when {
            player == null -> PlayerDetailUiState.NotFound
            else -> PlayerDetailUiState.Success(player, matches)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerDetailUiState.Loading
    )

    fun deleteMatch(match: Match) {
        viewModelScope.launch {
            repository.deleteMatch(match)
        }
    }
}
