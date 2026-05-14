package com.example.faceit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.faceit.R
import com.example.faceit.model.MatchResult
import com.example.faceit.ui.viewmodel.AddMatchViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMatchScreen(
    onNavigateUp: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddMatchViewModel = hiltViewModel()
) {
    val player by viewModel.player.collectAsStateWithLifecycle()
    val mapName by viewModel.mapName.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val kills by viewModel.kills.collectAsStateWithLifecycle()
    val deaths by viewModel.deaths.collectAsStateWithLifecycle()
    val dateMillis by viewModel.dateMillis.collectAsStateWithLifecycle()
    val titleRes = if (viewModel.isEditMode) R.string.edit_match_title else R.string.add_match_title

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = player?.nickname?.let { stringResource(R.string.add_match_for_player, it) }
                    ?: stringResource(R.string.player_loading)
            )
            OutlinedTextField(
                value = mapName,
                onValueChange = viewModel::setMapName,
                label = { Text(stringResource(R.string.field_map)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(stringResource(R.string.field_result), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = result == MatchResult.WIN,
                    onClick = { viewModel.setResult(MatchResult.WIN) },
                    label = { Text(stringResource(R.string.result_win)) }
                )
                FilterChip(
                    selected = result == MatchResult.LOSS,
                    onClick = { viewModel.setResult(MatchResult.LOSS) },
                    label = { Text(stringResource(R.string.result_loss)) }
                )
            }

            OutlinedTextField(
                value = kills,
                onValueChange = viewModel::setKills,
                label = { Text(stringResource(R.string.field_kills)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = deaths,
                onValueChange = viewModel::setDeaths,
                label = { Text(stringResource(R.string.field_deaths)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            val dateText = remember(dateMillis) {
                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru-RU")).format(Date(dateMillis))
            }
            Text(text = stringResource(R.string.field_date_value, dateText))
            Button(
                onClick = { viewModel.setDateMillis(System.currentTimeMillis()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_set_now_date))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.saveMatch(onSaved) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_save_match))
            }
        }
    }
}
