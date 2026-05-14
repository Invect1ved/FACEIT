package com.example.faceit.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.faceit.data.repository.PlayerRepository
import com.example.faceit.model.Player
import com.example.faceit.ui.navigation.PlayerFormRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerFormState(
    val nickname: String = "",
    val elo: String = "",
    val level: String = "",
    val faceitUrl: String = "",
    val comment: String = ""
)

@HiltViewModel
class PlayerFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PlayerRepository
) : ViewModel() {

    private val routePlayerId: Long = savedStateHandle.toRoute<PlayerFormRoute>().playerId

    val isEditMode: Boolean = routePlayerId != 0L

    private val _form = MutableStateFlow(PlayerFormState())
    val form: StateFlow<PlayerFormState> = _form.asStateFlow()

    init {
        if (isEditMode) {
            viewModelScope.launch {
                repository.observePlayer(routePlayerId).collect { player ->
                    if (player != null) {
                        _form.value = PlayerFormState(
                            nickname = player.nickname,
                            elo = player.elo.toString(),
                            level = player.level.toString(),
                            faceitUrl = player.faceitUrl,
                            comment = player.comment
                        )
                    }
                }
            }
        }
    }

    fun setNickname(value: String) {
        _form.update { it.copy(nickname = value) }
    }

    fun setElo(value: String) {
        _form.update { it.copy(elo = value.filter { ch -> ch.isDigit() }.take(5)) }
    }

    fun setLevel(value: String) {
        _form.update { it.copy(level = value.filter { ch -> ch.isDigit() }.take(3)) }
    }

    fun setFaceitUrl(value: String) {
        _form.update { it.copy(faceitUrl = value) }
    }

    fun setComment(value: String) {
        _form.update { it.copy(comment = value) }
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            val f = _form.value
            val elo = f.elo.toIntOrNull() ?: 0
            val level = f.level.toIntOrNull() ?: 1
            val player = Player(
                id = if (isEditMode) routePlayerId else 0L,
                nickname = f.nickname.trim().ifEmpty { "Игрок" },
                elo = elo,
                level = level.coerceAtLeast(1),
                faceitUrl = f.faceitUrl.trim(),
                comment = f.comment.trim()
            )
            if (isEditMode) {
                repository.updatePlayer(player)
            } else {
                repository.insertPlayer(player)
            }
            onDone()
        }
    }
}
