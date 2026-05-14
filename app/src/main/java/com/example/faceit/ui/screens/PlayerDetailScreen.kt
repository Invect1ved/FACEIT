package com.example.faceit.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.faceit.R
import com.example.faceit.model.Match
import com.example.faceit.model.MatchResult
import com.example.faceit.model.Player
import com.example.faceit.ui.viewmodel.PlayerDetailUiState
import com.example.faceit.ui.viewmodel.PlayerDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(
    onNavigateUp: () -> Unit,
    onEditPlayer: (Long) -> Unit,
    onAddMatch: (Long) -> Unit,
    onEditMatch: (playerId: Long, matchId: Long) -> Unit,
    viewModel: PlayerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDeleteMatch by remember { mutableStateOf<Match?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.player_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (uiState is PlayerDetailUiState.Success) {
                        val id = (uiState as PlayerDetailUiState.Success).player.id
                        IconButton(onClick = { onEditPlayer(id) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.icon_edit_player_cd))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is PlayerDetailUiState.Success) {
                val id = (uiState as PlayerDetailUiState.Success).player.id
                FloatingActionButton(onClick = { onAddMatch(id) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.fab_add_match_cd))
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            PlayerDetailUiState.Loading -> {
                BoxLoading(Modifier.padding(padding))
            }

            PlayerDetailUiState.NotFound -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.player_not_found))
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onNavigateUp) {
                        Text(stringResource(R.string.btn_back_to_list))
                    }
                }
            }

            is PlayerDetailUiState.Success -> {
                val pid = state.player.id
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        PlayerStatsCard(state.player)
                    }
                    item {
                        Text(
                            text = stringResource(R.string.recent_matches_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (state.matches.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.matches_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(state.matches, key = { it.id }) { match ->
                            MatchCard(
                                match = match,
                                onEdit = { onEditMatch(pid, match.id) },
                                onDelete = { pendingDeleteMatch = match }
                            )
                        }
                    }
                }
            }
        }
    }

    pendingDeleteMatch?.let { match ->
        AlertDialog(
            onDismissRequest = { pendingDeleteMatch = null },
            title = { Text(stringResource(R.string.dialog_delete_match_title)) },
            text = { Text(stringResource(R.string.dialog_delete_match_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMatch(match)
                        pendingDeleteMatch = null
                    }
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteMatch = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun BoxLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PlayerStatsCard(player: Player) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(player.nickname, style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.stat_elo, player.elo))
            Text(stringResource(R.string.stat_level, player.level))
            if (player.faceitUrl.isNotBlank()) {
                Text(stringResource(R.string.stat_url, player.faceitUrl), style = MaterialTheme.typography.bodySmall)
            }
            if (player.comment.isNotBlank()) {
                Text(stringResource(R.string.stat_comment, player.comment))
            }
        }
    }
}

@Composable
private fun MatchCard(match: Match, onEdit: () -> Unit, onDelete: () -> Unit) {
    val dateStr = remember(match.date) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru-RU")).format(Date(match.date))
    }
    val resultLabel = when (match.result) {
        MatchResult.WIN -> stringResource(R.string.result_win)
        MatchResult.LOSS -> stringResource(R.string.result_loss)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(match.mapName, style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.match_line_kd, match.kills, match.deaths),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    stringResource(R.string.match_line_meta, resultLabel, dateStr),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.icon_edit_match_cd))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.icon_delete_match_cd))
            }
        }
    }
}
