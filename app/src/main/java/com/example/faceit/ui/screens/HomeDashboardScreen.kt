package com.example.faceit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.faceit.R
import com.example.faceit.model.MatchResult
import com.example.faceit.model.Player
import com.example.faceit.ui.components.EloLineChart
import com.example.faceit.ui.components.PlayerRosterList
import com.example.faceit.ui.viewmodel.HomeDashboardContent
import com.example.faceit.ui.viewmodel.HomeDashboardUiState
import com.example.faceit.ui.viewmodel.HomeDashboardViewModel
import com.example.faceit.ui.viewmodel.MapStat
import com.example.faceit.ui.viewmodel.MatchHistoryRow
import com.example.faceit.ui.viewmodel.PlayersListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    onOpenMyProfile: (Long) -> Unit,
    onOpenPlayer: (Long) -> Unit,
    onAddPlayer: () -> Unit,
    onAddMatch: (Long) -> Unit,
    viewModel: HomeDashboardViewModel = hiltViewModel(),
    playersListViewModel: PlayersListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var pendingDeletePlayer by remember { mutableStateOf<Player?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) }
            )
        },
        floatingActionButton = {
            val ready = uiState as? HomeDashboardUiState.Ready
            val pid = ready?.content?.playerId
            when {
                tabIndex == 2 -> {
                    FloatingActionButton(onClick = onAddPlayer) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.fab_add_player_cd)
                        )
                    }
                }
                pid != null -> {
                    FloatingActionButton(onClick = { onAddMatch(pid) }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.fab_add_match_cd)
                        )
                    }
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            HomeDashboardUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            is HomeDashboardUiState.Ready -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    PrimaryTabRow(selectedTabIndex = tabIndex) {
                        Tab(
                            selected = tabIndex == 0,
                            onClick = { tabIndex = 0 },
                            text = { Text(stringResource(R.string.tab_overview)) }
                        )
                        Tab(
                            selected = tabIndex == 1,
                            onClick = { tabIndex = 1 },
                            text = { Text(stringResource(R.string.tab_history)) }
                        )
                        Tab(
                            selected = tabIndex == 2,
                            onClick = { tabIndex = 2 },
                            text = { Text(stringResource(R.string.tab_roster)) }
                        )
                    }
                    HorizontalDivider()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (tabIndex) {
                            0 -> OverviewTab(content = state.content, onOpenMyProfile = onOpenMyProfile)
                            1 -> HistoryTab(content = state.content)
                            2 -> SquadTab(
                                roster = state.content.roster,
                                dashboardPlayerId = state.content.playerId,
                                onOpenPlayer = onOpenPlayer,
                                onDeleteSwipe = { playersListViewModel.deletePlayer(it) },
                                onDeleteClick = { pendingDeletePlayer = it }
                            )
                        }
                    }
                }
            }
        }
    }

    pendingDeletePlayer?.let { player ->
        AlertDialog(
            onDismissRequest = { pendingDeletePlayer = null },
            title = { Text(stringResource(R.string.dialog_delete_player_title)) },
            text = { Text(stringResource(R.string.dialog_delete_player_message, player.nickname)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        playersListViewModel.deletePlayer(player)
                        pendingDeletePlayer = null
                    }
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePlayer = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun SquadTab(
    roster: List<Player>,
    dashboardPlayerId: Long?,
    onOpenPlayer: (Long) -> Unit,
    onDeleteSwipe: (Player) -> Unit,
    onDeleteClick: (Player) -> Unit
) {
    if (roster.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.squad_empty_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        PlayerRosterList(
            players = roster,
            highlightPlayerId = dashboardPlayerId,
            onOpenPlayer = onOpenPlayer,
            onDeletePlayer = onDeleteSwipe,
            onDeletePlayerClick = onDeleteClick
        )
    }
}

@Composable
private fun OverviewTab(
    content: HomeDashboardContent,
    onOpenMyProfile: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { MyEloCard(content, onOpenMyProfile) }
        item {
            SectionTitle(stringResource(R.string.home_maps_title))
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                content.maps.forEach { m -> MapRow(m) }
            }
        }
        item {
            SectionTitle(stringResource(R.string.home_elo_chart_title))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_elo_chart_hint_v2),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            EloLineChart(points = content.eloHistory)
        }
        item {
            SectionTitle(stringResource(R.string.home_stats_title))
            Spacer(modifier = Modifier.height(8.dp))
            StatsGrid(content)
        }
    }
}

@Composable
private fun HistoryTab(content: HomeDashboardContent) {
    if (content.matchHistory.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.history_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                content.matchHistory,
                key = { "${it.mapName}_${it.dateMillis}_${it.eloAfter}" }
            ) { row ->
                HistoryMatchCard(row)
            }
        }
    }
}

@Composable
private fun HistoryMatchCard(row: MatchHistoryRow) {
    val dateStr = rememberDate(row.dateMillis)
    val resultLabel = when (row.result) {
        MatchResult.WIN -> stringResource(R.string.result_win)
        MatchResult.LOSS -> stringResource(R.string.result_loss)
    }
    val deltaColor = if (row.eloDelta >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(row.mapName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                stringResource(R.string.history_line_result_kd, resultLabel, row.kills, row.deaths),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val deltaText = if (row.eloDelta > 0) "+${row.eloDelta}" else "${row.eloDelta}"
                Text(
                    text = stringResource(R.string.history_elo_change, deltaText, row.eloAfter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = deltaColor
                )
            }
        }
    }
}

@Composable
private fun rememberDate(ms: Long): String {
    return remember(ms) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru-RU")).format(Date(ms))
    }
}

@Composable
private fun MyEloCard(content: HomeDashboardContent, onOpenMyProfile: (Long) -> Unit) {
    val modifier = if (content.playerId != null) {
        Modifier
            .fillMaxWidth()
            .clickable { onOpenMyProfile(content.playerId) }
    } else {
        Modifier.fillMaxWidth()
    }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.home_my_elo_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "${content.elo}",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 52.sp
                    )
                }
                if (content.playerId != null) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = stringResource(R.string.home_open_profile_cd),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_my_nick_level, content.nickname, content.level),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (content.isPlaceholder) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.home_placeholder_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun MapRow(m: MapStat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(m.mapName, style = MaterialTheme.typography.bodyLarge)
        Text(
            stringResource(R.string.home_map_matches, m.matches),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StatsGrid(content: HomeDashboardContent) {
    val kdStr = String.format(Locale.US, "%.2f", content.kd)
    val adrStr = String.format(Locale.US, "%.1f", content.adr)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCell(stringResource(R.string.home_stat_kd), kdStr, Modifier.weight(1f))
            StatCell(stringResource(R.string.home_stat_wr), "${content.winRatePercent}%", Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCell(stringResource(R.string.home_stat_hs), "${content.hsPercent}%", Modifier.weight(1f))
            StatCell(stringResource(R.string.home_stat_adr), adrStr, Modifier.weight(1f))
        }
        StatCell(
            label = stringResource(R.string.home_stat_record),
            value = stringResource(R.string.home_stat_wl, content.wins, content.losses),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
